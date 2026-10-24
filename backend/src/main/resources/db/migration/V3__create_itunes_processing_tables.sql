CREATE TABLE itunes_track_processing (
	id BIGINT PRIMARY KEY,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_itunes_track_processing_track
		FOREIGN KEY (id) REFERENCES itunes_track (id)
);

CREATE TABLE itunes_service_id_to_itunes_track (
	service_id VARCHAR(255) PRIMARY KEY,
	track_id BIGINT NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_itunes_service_id_to_itunes_track_track
		FOREIGN KEY (track_id) REFERENCES itunes_track (id)
);
