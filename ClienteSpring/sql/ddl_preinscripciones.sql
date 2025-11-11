-- DDL para la tabla preinscripciones
CREATE TABLE IF NOT EXISTS preinscripciones (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  apellido VARCHAR(120),
  fecha_nacimiento DATE NOT NULL,
  telefono VARCHAR(30),
  email VARCHAR(180) NOT NULL,
  motivo TEXT,
  estado VARCHAR(20) DEFAULT 'VIGENTE',
  motivo_rechazo TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_preinscripcion_email UNIQUE (email)
);
