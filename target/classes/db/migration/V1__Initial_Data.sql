-- V1__Initial_Data.sql - Contiene la creación de tablas y los datos iniciales

-- Tabla de Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla de Usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Unión para Usuarios y Roles (Relación Muchos a Muchos)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabla de Servicios
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL, -- Duración del servicio en minutos
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Citas (Appointments)
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    stylist_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    appointment_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    FOREIGN KEY (client_id) REFERENCES users(id),
    FOREIGN KEY (stylist_id) REFERENCES users(id), -- Asumiendo que los estilistas son usuarios
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Datos Iniciales --

-- Roles iniciales
INSERT INTO roles (name) VALUES ('ROLE_CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_STYLIST') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

-- Contraseña de ejemplo para todos los usuarios: 'password123'
-- Hash Bcrypt: '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6'

-- Usuario Administrador
INSERT INTO users (username, password, email, full_name, phone_number, enabled) VALUES ('admin', '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6', 'admin@essencedetoi.com', 'Admin Principal', '000000000', true);

-- Estilistas
INSERT INTO users (username, password, email, full_name, phone_number, enabled) VALUES ('estilista.ana', '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6', 'ana.g@essencedetoi.com', 'Ana Gómez', '111111111', true);
INSERT INTO users (username, password, email, full_name, phone_number, enabled) VALUES ('estilista.carlos', '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6', 'carlos.p@essencedetoi.com', 'Carlos Pérez', '222222222', true);
INSERT INTO users (username, password, email, full_name, phone_number, enabled) VALUES ('estilista.sofia', '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6', 'sofia.m@essencedetoi.com', 'Sofía Martínez', '333333333', true);

-- Usuario Cliente
INSERT INTO users (username, password, email, full_name, phone_number, enabled) VALUES ('cliente.laura', '$2a$10$Ih7.mjdYeAryY4.OJmdiNe9GAq0jzIDqd8TYBfQS65iOcyBYs2um6', 'laura.v@example.com', 'Laura Vega', '444444444', true);

-- Asignación de Roles
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username='admin'), (SELECT id from roles where name='ROLE_ADMIN'));
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username='estilista.ana'), (SELECT id from roles where name='ROLE_STYLIST'));
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username='estilista.carlos'), (SELECT id from roles where name='ROLE_STYLIST'));
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username='estilista.sofia'), (SELECT id from roles where name='ROLE_STYLIST'));
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username='cliente.laura'), (SELECT id from roles where name='ROLE_CLIENT'));

-- Ejemplo de servicios
INSERT INTO services (name, description, price, duration_minutes) VALUES ('Corte de Cabello Damas', 'Incluye lavado y secado básico', 25.00, 60);
INSERT INTO services (name, description, price, duration_minutes) VALUES ('Manicura Clásica', 'Limado, cutículas y esmaltado', 15.00, 45);
INSERT INTO services (name, description, price, duration_minutes) VALUES ('Tinturación del Cabello', 'Aplicación profesional de tinte para cambiar o realzar el color del cabello, incluyendo asesoría personalizada.', 85.00, 90);
