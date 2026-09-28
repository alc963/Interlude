CREATE TABLE itunes_artist (
	id BIGINT PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	link_url VARCHAR(200) NOT NULL UNIQUE,
	amg_artist_id BIGINT,
	primary_genre_name VARCHAR(80),
	primary_genre_id BIGINT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	modified_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE itunes_collection (
	id BIGINT PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	censored_name VARCHAR(200),
	view_url VARCHAR(200) NOT NULL UNIQUE,
	artwork_url_60 VARCHAR(200),
	artwork_url_100 VARCHAR(200),
	price DOUBLE PRECISION,
	explicitness VARCHAR(20),
	disc_count INTEGER,
	disc_number INTEGER,
	track_count INTEGER,
	copyright VARCHAR(500),
	content_advisory_rating VARCHAR(20),
	country VARCHAR(10),
	currency VARCHAR(10),
	release_date TIMESTAMP WITH TIME ZONE,
	primary_genre_name VARCHAR(80),
	artist_id BIGINT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_itunes_track_artist
		FOREIGN KEY (artist_id) REFERENCES itunes_artist (id)
);

CREATE TABLE itunes_track (
	id BIGINT PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	censored_name VARCHAR(200),
	view_url VARCHAR(300) NOT NULL UNIQUE,
	preview_url VARCHAR(500),
	artwork_url_60 VARCHAR(200),
	artwork_url_100 VARCHAR(200),
	price DOUBLE PRECISION,
	explicitness VARCHAR(20),
	disc_count INTEGER,
	disc_number INTEGER,
	track_count INTEGER,
	track_number INTEGER,
	track_time_millis BIGINT,
	is_streamable BOOLEAN,
	country VARCHAR(10),
	currency VARCHAR(10),
	release_date TIMESTAMP WITH TIME ZONE,
	primary_genre_name VARCHAR(80),
	artist_id BIGINT,
	collection_id BIGINT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_itunes_track_artist
	    FOREIGN KEY (artist_id) REFERENCES itunes_artist (id),
	CONSTRAINT fk_itunes_track_collection
		FOREIGN KEY (collection_id) REFERENCES itunes_collection (id)
);
