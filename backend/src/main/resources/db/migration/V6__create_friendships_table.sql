CREATE TABLE friendships (
	id BIGSERIAL PRIMARY KEY,
	user_requested_id BIGINT NOT NULL,
	user_recipient_id BIGINT NOT NULL,
	status VARCHAR(16) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	accepted_at TIMESTAMP WITH TIME ZONE,
	CONSTRAINT fk_friendships_user_requested
		FOREIGN KEY (user_requested_id) REFERENCES users (id) ON DELETE CASCADE,
	CONSTRAINT fk_friendships_user_recipient
		FOREIGN KEY (user_recipient_id) REFERENCES users (id) ON DELETE CASCADE,
	CONSTRAINT chk_friendships_not_self CHECK (user_requested_id <> user_recipient_id),
	CONSTRAINT chk_friendships_status CHECK (status IN ('PENDING', 'ACCEPTED'))
);

CREATE UNIQUE INDEX uq_friendships_pair ON friendships (LEAST(user_requested_id, user_recipient_id), GREATEST(user_requested_id, user_recipient_id));
CREATE INDEX idx_friendships_user_recipient ON friendships (user_recipient_id);
