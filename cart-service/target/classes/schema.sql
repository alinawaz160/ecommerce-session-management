-- Cart table linked to guest session
CREATE TABLE IF NOT EXISTS cart (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(255) NOT NULL UNIQUE,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
);

-- Cart items within a cart
CREATE TABLE IF NOT EXISTS cart_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id      BIGINT         NOT NULL,
    product_id   VARCHAR(255)   NOT NULL,
    product_name VARCHAR(500)   NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    quantity     INT            NOT NULL DEFAULT 1,
    image_url    VARCHAR(1000),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
    UNIQUE KEY uq_cart_product (cart_id, product_id),
    INDEX idx_cart_id (cart_id)
);
