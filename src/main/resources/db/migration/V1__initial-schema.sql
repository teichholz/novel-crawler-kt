CREATE TABLE IF NOT EXISTS crawler.novels (
    id SERIAL PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    img VARCHAR(255) NOT NULL,
    details_href VARCHAR(255) NOT NULL,
    site_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS crawler.chapters (
    id SERIAL PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    content_href VARCHAR(255) NOT NULL,
    release_date TIMESTAMP NOT NULL,
    novel INT NOT NULL,
    CONSTRAINT fk_chapters_novel__id FOREIGN KEY (novel) REFERENCES novels(id) ON delete RESTRICT ON update RESTRICT
);