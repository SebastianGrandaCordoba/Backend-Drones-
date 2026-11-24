"""Prueba de humo de la API (HU-01 a HU-13) contra la aplicación en ejecución.

Recorre los criterios de aceptación de punta a punta: registra drones y misiones,
asigna con ambas estrategias, valida errores 400/404/409 y el historial.
Úsela con una base de datos vacía, por ejemplo:

    MONGODB_URI=mongodb://localhost:27017/drones_prueba java -jar target/centro-control-drones-1.0.0.jar
    python3 scripts/prueba-de-humo.py

Solo usa la biblioteca estándar de Python. API_URL cambia la dirección (por defecto http://127.0.0.1:8080/api).
"""
import datetime
import json
import os
import sys
import urllib.error
import urllib.request

B = os.environ.get("API_URL", "http://127.0.0.1:8080/api")
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
fallos = []


def req(m, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(B + path, data=data, method=m, headers={"Content-Type": "application/json"})
    try:
        with opener.open(r) as resp:
            t = resp.read().decode()
            return resp.status, (json.loads(t) if t else None)
    except urllib.error.HTTPError as e:
        t = e.read().decode()
        return e.code, (json.loads(t) if t else None)


def check(nombre, cond, extra=""):
    print(("OK   " if cond else "FAIL ") + nombre + ("" if cond else f"  -> {extra}"))
    if not cond:
        fallos.append(nombre)


fut = (datetime.datetime.now() + datetime.timedelta(days=2)).strftime("%Y-%m-%dT%H:%M:%S")
fut2 = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S")
ub = lambda la, lo: {"latitud": la, "longitud": lo}

# HU-13 reserva
s, b = req("GET", "/configuracion/reserva"); check("HU-13 reserva sin configurar", s == 200 and b["configurada"] is False, b)
s, b = req("POST", "/asignaciones", {"misionId": "x"}); check("Asignar sin misión existente -> 404", s in (404, 409), (s, b))
s, b = req("PUT", "/configuracion/reserva", {"valor": 5, "unidad": "mi"}); check("HU-13 unidad inválida 400", s == 400, (s, b))
s, b = req("PUT", "/configuracion/reserva", {"valor": -1, "unidad": "km"}); check("HU-13 reserva negativa 400", s == 400, (s, b))
s, b = req("PUT", "/configuracion/reserva", {"valor": 5, "unidad": "km"}); check("HU-13 configurar reserva", s == 200 and b["valor"] == 5, (s, b))

# HU-01 drones
d = lambda i, t, bat, aut, cap, la, lo, est="DISPONIBLE": {"id": i, "tipo": t, "bateria": bat, "autonomiaKm": aut, "capacidad": cap, "estado": est, "ubicacion": ub(la, lo)}
s, b = req("POST", "/drones", d("E-1", "ENTREGA", 80, 40, 5, 4.60, -74.08)); check("HU-01 registrar E-1", s == 201 and b["unidadCapacidad"], (s, b))
s, b = req("POST", "/drones", d("E-2", "ENTREGA", 95, 40, 5, 4.70, -74.05)); check("HU-01 registrar E-2", s == 201, (s, b))
s, b = req("POST", "/drones", d("F-1", "FOTOGRAFIA", 90, 30, 64, 4.60, -74.08)); check("HU-01 registrar F-1", s == 201, (s, b))
s, b = req("POST", "/drones", d("E-1", "ENTREGA", 80, 40, 5, 4.6, -74.08)); check("HU-01 id duplicado 409", s == 409, (s, b))
s, b = req("POST", "/drones", d("E-9", "ENTREGA", 120, 40, 5, 4.6, -74.08)); check("HU-01 batería>100 400", s == 400, (s, b))
s, b = req("POST", "/drones", d("E-9", "ENTREGA", 50, 40, 5, 4.6, -74.08, "ASIGNADO")); check("HU-01 estado inicial inválido 400", s == 400, (s, b))
s, b = req("POST", "/drones", {"id": "E-9"}); check("HU-01 campos faltantes 400", s == 400 and b["detalles"], (s, b))

# HU-02
s, b = req("GET", "/drones?tipo=ENTREGA"); check("HU-02 filtrar por tipo", s == 200 and len(b) == 2, b)
s, b = req("GET", "/drones?estado=DISPONIBLE&tipo=FOTOGRAFIA"); check("HU-02 filtrar tipo+estado", s == 200 and len(b) == 1, b)
s, b = req("GET", "/drones?tipo=NADA"); check("HU-02 tipo inválido 400", s == 400, (s, b))
s, b = req("GET", "/drones/NO-EXISTE"); check("HU-02 dron inexistente 404", s == 404, (s, b))

# HU-03
s, b = req("PUT", "/drones/E-2/datos-operativos", {"bateria": 99, "autonomiaKm": 45, "ubicacion": ub(4.71, -74.06)}); check("HU-03 actualizar datos", s == 200 and b["bateria"] == 99, (s, b))
s, b = req("PUT", "/drones/F-1/estado", {"estado": "MANTENIMIENTO"}); check("HU-03 a mantenimiento", s == 200 and b["estado"] == "MANTENIMIENTO", (s, b))
s, b = req("PUT", "/drones/F-1/estado", {"estado": "EN_MISION"}); check("HU-03 estado no permitido 400", s == 400, (s, b))

# HU-04
m = lambda t, p, dist, lim, la, lo, cap: {"tipo": t, "prioridad": p, "distanciaKm": dist, "limiteTiempo": lim, "destino": ub(la, lo), "capacidadRequerida": cap}
s, m1 = req("POST", "/misiones", m("ENTREGA", "MEDIA", 10, fut, 4.61, -74.07, 3)); check("HU-04 registrar misión", s == 201 and m1["estado"] == "PENDIENTE", (s, m1))
s, m2 = req("POST", "/misiones", m("ENTREGA", "CRITICA", 10, fut, 4.71, -74.05, 3)); check("HU-04 registrar misión crítica", s == 201, (s, m2))
s, m3 = req("POST", "/misiones", m("FOTOGRAFIA", "CRITICA", 5, fut2, 4.6, -74.0, 10)); check("HU-04 misión foto", s == 201, (s, m3))
s, b = req("POST", "/misiones", m("ENTREGA", "ALTA", 10, "2020-01-01T00:00:00", 4.6, -74, 1)); check("HU-04 límite pasado 400", s == 400, (s, b))
s, b = req("POST", "/misiones", m("ENTREGA", "ALTA", -3, fut, 4.6, -74, 1)); check("HU-04 distancia negativa 400", s == 400, (s, b))

# HU-05
s, b = req("GET", "/misiones/pendientes"); ids = [x["id"] for x in b]
check("HU-05 orden prioridad y plazo", ids[:2] == [m3["id"], m2["id"]] and ids[-1] == m1["id"], [(x["prioridad"], x["limiteTiempo"]) for x in b])

# HU-07
s, b = req("GET", "/asignaciones/estrategias"); check("HU-07 estrategias", s == 200 and len(b) == 2 and b[0]["predeterminada"], b)

# HU-06/08 asignar con MENOR_DISTANCIA (m2 destino cerca de E-2)
s, b = req("POST", "/asignaciones", {"misionId": m2["id"]}); check("HU-06 asignar m2", s == 201 and b["asignacion"]["dronId"] == "E-2", (s, b))
check("HU-08 justificación", s == 201 and "E-2" in b["asignacion"]["justificacion"], b)
s, b = req("GET", f"/asignaciones/mision/{m2['id']}"); check("HU-08 detalle", s == 200 and b["asignada"] and b["estadoMision"] == "ASIGNADA", b)
s, b = req("GET", "/drones/E-2"); check("HU-06 dron pasa a ASIGNADO", b["estado"] == "ASIGNADO", b)

# HU-10 no doble asignación
s, b = req("POST", "/asignaciones", {"misionId": m2["id"]}); check("HU-10 misión ya asignada 409", s == 409, (s, b))

# HU-07 estrategia mayor batería con m1: solo E-1 disponible
s, b = req("POST", "/asignaciones", {"misionId": m1["id"], "estrategia": "mayor_bateria"}); check("HU-07 mayor batería", s == 201 and b["asignacion"]["estrategia"] == "MAYOR_BATERIA" and b["asignacion"]["dronId"] == "E-1", (s, b))
s, b = req("POST", "/asignaciones", {"misionId": m3["id"], "estrategia": "RAPIDA"}); check("HU-07 estrategia inexistente 400", s == 400, (s, b))

# HU-09 sin candidato (F-1 en mantenimiento)
s, b = req("POST", "/asignaciones", {"misionId": m3["id"]}); check("HU-09 sin candidato 200 con motivos", s == 200 and not b["asignado"] and b["motivos"], (s, b))
s, b = req("GET", f"/misiones/{m3['id']}"); check("HU-09 misión sigue PENDIENTE", b["estado"] == "PENDIENTE", b)
s, b = req("GET", f"/asignaciones/mision/{m3['id']}"); check("HU-08 detalle sin asignar", s == 200 and not b["asignada"], b)

# HU-11 ciclo de vida
s, b = req("POST", f"/misiones/{m2['id']}/completar"); check("HU-11 completar sin iniciar 409", s == 409, (s, b))
s, b = req("PUT", "/drones/E-2/estado", {"estado": "MANTENIMIENTO"}); check("HU-03 dron con misión no va a mantenimiento 409", s == 409, (s, b))
s, b = req("POST", f"/misiones/{m2['id']}/iniciar"); check("HU-11 iniciar", s == 200 and b["estado"] == "EN_EJECUCION", (s, b))
s, b = req("GET", "/drones/E-2"); check("HU-11 dron EN_MISION", b["estado"] == "EN_MISION", b)
s, b = req("POST", f"/misiones/{m2['id']}/cancelar"); check("HU-11 cancelar en ejecución 409", s == 409, (s, b))
s, b = req("POST", f"/misiones/{m2['id']}/completar"); check("HU-11 completar", s == 200 and b["estado"] == "COMPLETADA", (s, b))
s, b = req("GET", "/drones/E-2"); check("HU-11 dron liberado", b["estado"] == "DISPONIBLE", b)
s, b = req("POST", f"/misiones/{m1['id']}/cancelar"); check("HU-11 cancelar asignada", s == 200 and b["estado"] == "CANCELADA", (s, b))
s, b = req("GET", "/drones/E-1"); check("HU-11 dron liberado al cancelar", b["estado"] == "DISPONIBLE", b)
s, b = req("POST", f"/misiones/{m3['id']}/iniciar"); check("HU-11 iniciar pendiente 409", s == 409, (s, b))
s, b = req("POST", "/misiones/NOPE/iniciar"); check("HU-11 misión inexistente 404", s == 404, (s, b))

# HU-12 historial
s, b = req("GET", "/asignaciones"); check("HU-12 historial completo", s == 200 and len(b) == 2 and all(not a["activa"] for a in b), b)
s, b = req("GET", "/asignaciones?dronId=E-2"); check("HU-12 filtro por dron", len(b) == 1 and b[0]["estadoMision"] == "COMPLETADA", b)
s, b = req("GET", f"/asignaciones?misionId={m1['id']}"); check("HU-12 filtro por misión", len(b) == 1 and b[0]["estadoMision"] == "CANCELADA", b)
s, b = req("GET", f"/asignaciones/mision/{m2['id']}"); check("HU-08 detalle de misión completada", s == 200, b)

print("\nFallos:", fallos if fallos else "ninguno")
sys.exit(1 if fallos else 0)
