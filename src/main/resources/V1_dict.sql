insert into bonus_rules (id, condition, reward_type, reward_value, priority, active) values
('052909d3-8507-4d2a-ac07-f121a7c5b8ea', '{"categories": ["PHARMACY", "SPORT"]}'::JSONB, 'FIXED', 50.00, 1, true),
('9a1e0fb8-ce29-45f7-87e6-5bf1afe9f8b7', '{"categories": ["TRAVEL"], "minAmount": 1000}'::JSONB, 'PERCENT', 1.50, 2, true);