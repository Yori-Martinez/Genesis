CREATE DATABASE IF NOT EXISTS tienda_juegos_db;
USE tienda_juegos_db;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('JUGADOR', 'EDITOR', 'ADMIN') DEFAULT 'JUGADOR',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    avatar_contenido LONGBLOB
);

CREATE TABLE juegos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) DEFAULT 0.00,
    caratula_contenido LONGBLOB,
    zip_contenido LONGBLOB,
    estado ENUM('PENDIENTE', 'PUBLICADO', 'RECHAZADO') DEFAULT 'PENDIENTE',
    id_editor INT,
    fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_editor) REFERENCES usuarios(id)
);

CREATE TABLE biblioteca (
    id_usuario INT,
    id_juego INT,
    fecha_adquisicion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_juego),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_juego) REFERENCES juegos(id)
);

INSERT INTO usuarios (username, email, password, rol) VALUES
    ('GabeNewell', 'papigave@tienda.com', '123456', 'ADMIN'),
    ('ElDonJuan', 'editor@tienda.com', '1234', 'EDITOR'),
    ('Elpepe67', 'jugador@tienda.com', '1234', 'JUGADOR');