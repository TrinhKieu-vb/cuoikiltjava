-- Create database
CREATE DATABASE IF NOT EXISTS drink_dessert_management;
USE drink_dessert_management;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN DEFAULT TRUE,
    created_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()*1000)
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id INT,
    description TEXT,
    stock INT DEFAULT 0,
    image_path VARCHAR(255) DEFAULT NULL,
    created_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()*1000),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    created_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()*1000)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    order_date BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()*1000),
    updated_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()*1000),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Order details table
CREATE TABLE IF NOT EXISTS order_details (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Insert default users
INSERT INTO users (username, password, email, role, active) VALUES
('admin', '$2a$10$fQq8HpEJlgTrW3oKKhE9J.1zYAm7tPxKpVQq8XwwJ5jGJ9J0/W9ey', 'admin@shop.com', 'ADMIN', TRUE),
('user', '$2a$10$Q3gFCvG0zVvZNGVCLdL5DO1AeVpSM2k4nIv9LXZ9w4p8Z8XrP9K9.', 'user@shop.com', 'USER', TRUE);

-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Đồ uống nóng', 'Các loại đồ uống nóng như cà phê, trà'),
('Đồ uống lạnh', 'Các loại đồ uống lạnh như nước ngọt, sinh tố'),
('Tráng miệng', 'Các loại tráng miệng như bánh, kem'),
('Bánh mì', 'Các loại bánh mì');

-- Insert sample products
INSERT INTO products (name, price, category_id, description, stock, image_path) VALUES
('Cà phê đen', 25000, 1, 'Cà phê đen không đường', 100, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=150'),
('Cà phê sữa', 30000, 1, 'Cà phê có sữa', 100, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=150'),
('Trà nóng', 15000, 1, 'Trà nóng thơm ngon', 80, 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=150'),
('Nước cam vắt', 35000, 2, 'Nước cam vắt tươi', 50, 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=150'),
('Sinh tố dâu', 40000, 2, 'Sinh tố dâu tây', 60, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=150'),
('Bánh tiramisu', 50000, 3, 'Bánh tiramisu ngon lạ', 30, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=150'),
('Bánh chocolate', 45000, 3, 'Bánh chocolate thơm ngon', 40, 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=150'),
('Bánh mì nước cốt dừa', 20000, 4, 'Bánh mì nước cốt dừa', 80, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=150');

INSERT INTO customers (name, phone) VALUES
('Nguyễn Văn A', '0912345678'),
('Trần Thị B', '0923456789'),
('Lê Văn C', '0934567890');

-- Insert sample orders
INSERT INTO orders (customer_id, total_amount, status, order_date) VALUES
(1, 95000, 'COMPLETED', UNIX_TIMESTAMP()*1000),
(2, 155000, 'COMPLETED', UNIX_TIMESTAMP()*1000),
(3, 80000, 'PENDING', UNIX_TIMESTAMP()*1000);

-- Insert sample order details
INSERT INTO order_details (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 2, 25000, 50000),
(1, 2, 1, 30000, 30000),
(1, 3, 1, 15000, 15000),
(2, 4, 2, 35000, 70000),
(2, 5, 1, 40000, 40000),
(2, 6, 1, 50000, 50000),
(3, 7, 2, 45000, 90000);

-- Create indexes for better performance
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_detail_order ON order_details(order_id);
CREATE INDEX idx_order_detail_product ON order_details(product_id);

