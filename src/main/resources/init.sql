CREATE TABLE FUNKOS
(
    ID               LONG AUTO_INCREMENT PRIMARY KEY,
    COD              CHAR(36) NOT NULL     DEFAULT RANDOM_UUID(),
    ID2              LONG,
    NOMBRE           VARCHAR(255),
    MODELO           ENUM ('MARVEL', 'DISNEY', 'ANIME', 'OTROS'),
    PRECIO           DECIMAL(10, 2),
    FECHALANZAMIENTO DATE,
    CREATEDAT        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATEDAT        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);