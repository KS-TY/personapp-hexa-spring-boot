INSERT INTO 
	`persona_db`.`persona`(`cc`,`nombre`,`apellido`,`genero`,`edad`) 
VALUES
	(123456789,'Pepe','Perez','M',30),
	(987654321,'Pepito','Perez','M',null),
	(321654987,'Pepa','Juarez','F',30),
	(147258369,'Pepita','Juarez','F',10),
	(963852741,'Fede','Perez','M',18);

INSERT INTO 
	`persona_db`.`profesion`(`id`,`nom`,`des`) 
VALUES
	(1,'Ingeniero de Software','Desarrollo de aplicaciones y sistemas'),
	(2,'Médico','Atención médica y salud'),
	(3,'Abogado','Servicios legales y jurídicos'),
	(4,'Profesor','Educación y enseñanza'),
	(5,'Contador','Contabilidad y finanzas');

INSERT INTO 
	`persona_db`.`telefono`(`num`,`oper`,`duenio`) 
VALUES
	('3001234567','Claro',123456789),
	('3109876543','Movistar',321654987),
	('3157894561','Tigo',147258369),
	('3208523697','Claro',963852741),
	('3151478523','Movistar',987654321);