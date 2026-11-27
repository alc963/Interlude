CREATE TABLE spotify_currently_playing (
	user_id BIGINT PRIMARY KEY,
	track_uri VARCHAR(255) NOT NULL,
	track_name VARCHAR(500) NOT NULL,
	artist_name VARCHAR(500),
	album_name VARCHAR(500),
	duration_ms INTEGER,
	progress_ms INTEGER,
	is_playing BOOLEAN NOT NULL,
	modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_spotify_currently_playing_user
		FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE spotify_recently_played (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL,
	track_uri VARCHAR(255) NOT NULL,
	track_name VARCHAR(500) NOT NULL,
	artist_name VARCHAR(500),
	album_name VARCHAR(500),
	duration_ms INTEGER,
	played_at TIMESTAMP WITH TIME ZONE NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT uq_spotify_recently_played_user_played_at
		UNIQUE (user_id, played_at),
	CONSTRAINT fk_spotify_recently_played_user
		FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
