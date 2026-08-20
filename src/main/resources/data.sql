-- Insert Jackpots
INSERT INTO jackpot (id, name, current_amount, initial_amount, created_at_date, updated_at_date)
VALUES ('jackpot-fixed-01', 'Fixed Strategy Jackpot', 5000.00, 5000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO jackpot (id, name, current_amount, initial_amount, created_at_date, updated_at_date)
VALUES ('jackpot-linear-02', 'Linear Variable Jackpot', 2500.00, 2500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Contribution Configurations (1-to-1 with Jackpot due to UNIQUE constraint)
INSERT INTO jackpot_contribution_configuration (jackpot_id, strategy_type, start_amount, contribution_pct)
VALUES ('jackpot-fixed-01', 'FIXED', 5.00, 10.00);

INSERT INTO jackpot_contribution_configuration (jackpot_id, strategy_type, start_amount, max_amount, contribution_pct, min_contribution_pct)
VALUES ('jackpot-linear-02', 'VARIABLE', 2500.00, 100000.00, 15.00, 2.50);



-- Insert Reward Configurations (1-to-1 with Jackpot due to UNIQUE constraint)
INSERT INTO jackpot_reward_configuration (jackpot_id, strategy_type, start_amount, reward_probability_pct)
VALUES ('jackpot-fixed-01', 'FIXED', 2.00, 10.00);

INSERT INTO jackpot_reward_configuration (jackpot_id, strategy_type, start_amount, max_amount, reward_probability_pct)
VALUES ('jackpot-linear-02', 'VARIABLE', 2500.00, 50000.00, 0.50);
