CREATE TABLE koma(
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  skill VARCHAR(50) DEFAULT NULL,
  update_koma INT DEFAULT -1
);

CREATE TABLE KomaRule(
  id INT PRIMARY KEY AUTO_INCREMENT,
  koma_id INT,
  rule VARCHAR(50),
  FOREIGN KEY (koma_id) REFERENCES koma(id)
);

CREATE TABLE Deck(
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  sfen VARCHAR(255),
  cost INT DEFAULT 0
);

CREATE TABLE Player(
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  selected_deck_id INT DEFAULT NULL,
  FOREIGN KEY (selected_deck_id) REFERENCES Deck(id)
);

CREATE TABLE PlayerDeck(
  id INT PRIMARY KEY AUTO_INCREMENT,
  player_id INT NOT NULL,
  deck_id INT NOT NULL,
  is_owner BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (player_id) REFERENCES Player(id),
  FOREIGN KEY (deck_id) REFERENCES Deck(id),
  UNIQUE(player_id, deck_id)
);

CREATE TABLE PlayerKoma(
  id INT PRIMARY KEY AUTO_INCREMENT,
  player_id INT NOT NULL,
  koma_id INT NOT NULL,
  is_owner BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (player_id) REFERENCES Player(id),
  FOREIGN KEY (koma_id) REFERENCES koma(id),
  UNIQUE(player_id, koma_id)
);
