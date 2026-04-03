BEGIN;

TRUNCATE TABLE tickets, orders, flights, airlines RESTART IDENTITY CASCADE;

INSERT INTO airlines (id, name, iata_code, country, website_url) VALUES
  (1, 'Aeroflot', 'SU', 'Russia', 'https://www.aeroflot.ru'),
  (2, 'S7 Airlines', 'S7', 'Russia', 'https://www.s7.ru'),
  (3, 'Ural Airlines', 'U6', 'Russia', 'https://www.uralairlines.ru');

INSERT INTO flights (
  id,
  flight_number,
  airline_id,
  departure_airport,
  arrival_airport,
  departure_time,
  arrival_time,
  aircraft_type,
  base_price,
  available_seats
) VALUES
  (1, 'SU1001', 1, 'LED', 'SVO', '2026-04-10 09:00:00', '2026-04-10 10:30:00', 'Airbus A320', 6500.00, 118),
  (2, 'S71025', 2, 'SVO', 'AER', '2026-04-11 14:15:00', '2026-04-11 18:00:00', 'Boeing 737-800', 9200.00, 96),
  (3, 'U62003', 3, 'SVX', 'DME', '2026-04-12 07:45:00', '2026-04-12 10:05:00', 'Airbus A321', 7100.00, 140),
  (4, 'SU1450', 1, 'KZN', 'LED', '2026-04-13 16:20:00', '2026-04-13 18:25:00', 'Sukhoi Superjet 100', 5600.00, 72),
  (5, 'S71111', 2, 'LED', 'KGD', '2026-04-14 11:10:00', '2026-04-14 13:00:00', 'Airbus A319', 5900.00, 84);

-- user_id values must match security-users.xml
-- 1 = admin
-- 2 = ivan1
INSERT INTO orders (
  id,
  user_id,
  created_at,
  total_price,
  currency,
  status,
  payment_method,
  external_link
) VALUES
  (1, 2, '2026-04-03 12:00:00', 7600.00, 'RUB', 'PENDING', 'INTERNAL', NULL),
  (2, 2, '2026-04-03 12:10:00', 13200.00, 'RUB', 'PAID', 'EXTERNAL', 'https://partner.example.com/booking/ext-2002'),
  (3, 1, '2026-04-03 12:20:00', 8900.00, 'RUB', 'PAID', 'INTERNAL', 'https://partner.example.com/pay/int-3003'),
  (4, 1, '2026-04-03 12:30:00', 5600.00, 'RUB', 'CANCELED', 'INTERNAL', NULL);

INSERT INTO tickets (
  id,
  flight_id,
  order_id,
  seat_number,
  seat_class,
  has_baggage,
  passenger_name,
  passenger_passport
) VALUES
  (1, 1, 1, '12A', 'ECONOMY', true, 'Ivan Ivanov', '1234 567890'),
  (2, 2, 2, '2C', 'BUSINESS', true, 'Ivan Ivanov', '1234 567890'),
  (3, 3, 3, '7F', 'ECONOMY', false, 'Admin User', '4321 123456'),
  (4, 4, 4, '5B', 'ECONOMY', false, 'Administrator', '1111 222333');

SELECT setval('airlines_id_seq', COALESCE((SELECT MAX(id) FROM airlines), 1), true);
SELECT setval('flights_id_seq', COALESCE((SELECT MAX(id) FROM flights), 1), true);
SELECT setval('orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 1), true);
SELECT setval('tickets_id_seq', COALESCE((SELECT MAX(id) FROM tickets), 1), true);

COMMIT;
