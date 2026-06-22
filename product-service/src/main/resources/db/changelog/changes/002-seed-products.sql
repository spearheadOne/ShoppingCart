
--changeset abondar:002-seed-products
INSERT INTO products (id, name, img_url, price)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Keyboard', '/images/keyboard.png', 99.99),
    ('22222222-2222-2222-2222-222222222222', 'Mouse', '/images/mouse.png', 49.99),
    ('33333333-3333-3333-3333-333333333333', 'Monitor', '/images/monitor.png', 249.99),

    ('44444444-4444-4444-4444-444444444444', 'Laptop Stand', '/images/laptop-stand.png', 39.99),
    ('55555555-5555-5555-5555-555555555555', 'USB-C Hub', '/images/usb-c-hub.png', 59.99),
    ('66666666-6666-6666-6666-666666666666', 'Webcam', '/images/webcam.png', 79.99),
    ('77777777-7777-7777-7777-777777777777', 'Headphones', '/images/headphones.png', 129.99),
    ('88888888-8888-8888-8888-888888888888', 'Microphone', '/images/microphone.png', 89.99),
    ('99999999-9999-9999-9999-999999999999', 'Desk Lamp', '/images/desk-lamp.png', 34.99),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Office Chair', '/images/office-chair.png', 199.99),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Standing Desk', '/images/standing-desk.png', 399.99),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'External SSD 1TB', '/images/external-ssd.png', 149.99),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Wireless Charger', '/images/wireless-charger.png', 29.99),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Smart Speaker', '/images/smart-speaker.png', 119.99),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Graphics Tablet', '/images/graphics-tablet.png', 179.99);

--rollback DELETE FROM products;
