-- Insérer quelques données de test
INSERT INTO enseignants (id, nom, prenom, matricule, email, telephone, heures_max_hebdo) 
VALUES ('1', 'Dupont', 'Jean', 'M001', 'jean.dupont@ecole.fr', '0123456789', 18);

INSERT INTO matieres (id, code, nom, description)
VALUES ('1', 'MAT001', 'Mathématiques', 'Cours de mathématiques');

INSERT INTO classes (id, nom, niveau, filiere, effectif)
VALUES ('1', '6ème A', '6ème', 'Générale', 25);