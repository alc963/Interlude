ALTER TABLE spotify_recently_played ADD COLUMN artist_uri VARCHAR(255), ADD COLUMN album_uri VARCHAR(255);

CREATE INDEX idx_spotify_recently_played_user_artist
	ON spotify_recently_played (user_id, artist_uri);
