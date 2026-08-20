CREATE TABLE IF NOT EXISTS jackpot (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    current_amount DECIMAL(15,2) NOT NULL,
    initial_amount DECIMAL(15,2) NOT NULL,
    created_at_date TIMESTAMP NOT NULL,
    updated_at_date TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS jackpot_contribution_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jackpot_id VARCHAR(64) NOT NULL UNIQUE,
    strategy_type VARCHAR(32) NOT NULL,
    start_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    contribution_pct DECIMAL(5,2) NOT NULL,
    min_contribution_pct DECIMAL(5,2),
    CONSTRAINT jackpot_contribution_configuration_frn_key FOREIGN KEY (jackpot_id) REFERENCES jackpot (id) ON DELETE CASCADE    );

CREATE TABLE IF NOT EXISTS jackpot_reward_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jackpot_id VARCHAR(64) NOT NULL UNIQUE,
    strategy_type VARCHAR(32) NOT NULL,
    start_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    reward_probability_pct DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (jackpot_id) REFERENCES jackpot(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS jackpot_contribution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bet_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    jackpot_id VARCHAR(64) NOT NULL,
    stake_amount DECIMAL(15,2) NOT NULL,
    contribution_amount DECIMAL(15,2) NOT NULL,
    current_jackpot_amount DECIMAL(15,2) NOT NULL,
    created_at_date TIMESTAMP NOT NULL,
    FOREIGN KEY (jackpot_id) REFERENCES jackpot (id) ON DELETE CASCADE,
    UNIQUE (bet_id, user_id, jackpot_id)
    );

CREATE TABLE IF NOT EXISTS jackpot_reward (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bet_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    jackpot_id VARCHAR(64) NOT NULL,
    jackpot_reward_amount DECIMAL(15,2) NOT NULL,
    created_at_date TIMESTAMP NOT NULL,
    FOREIGN KEY (jackpot_id) REFERENCES jackpot(id) ON DELETE CASCADE,
    UNIQUE (bet_id,user_id, jackpot_id)
    );
