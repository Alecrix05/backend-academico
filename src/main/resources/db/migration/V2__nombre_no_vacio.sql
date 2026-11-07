-- Restricción nueva sin reescribir V1: la V1 ya aplicada queda intacta.
ALTER TABLE alumnos
    ADD CONSTRAINT ck_nombre_no_vacio CHECK (LENGTH(TRIM(nombre)) > 0);