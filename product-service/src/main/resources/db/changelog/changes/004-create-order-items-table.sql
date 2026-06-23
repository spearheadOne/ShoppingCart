--liquibase formatted sql

--changeset abondar:004-create-order-items-table

CREATE TABLE order_items (
                             order_id UUID NOT NULL,
                             product_id UUID NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             product_img_url VARCHAR(1024),
                             unit_price DECIMAL(19, 2) NOT NULL,
                             quantity INTEGER NOT NULL,
                             line_total DECIMAL(19, 2) NOT NULL,

                             CONSTRAINT pk_order_items
                                 PRIMARY KEY (order_id, product_id),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0),

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_line_total
                                 CHECK (line_total >= 0)
);

CREATE INDEX idx_order_items_order_id

    ON order_items(order_id);

CREATE INDEX idx_order_items_product_id

    ON order_items(product_id);

--rollback DROP TABLE order-items;