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

-- Insertar estudios por defecto
INSERT INTO 
	`persona_db`.`estudios`(`id_prof`,`cc_per`,`fecha`,`univer`) 
VALUES
	-- Pepe Perez (123456789) estudió Ingeniería de Software
	(1, 123456789, '2020-12-15', 'Universidad Javeriana'),
	
	-- Pepa Juarez (321654987) estudió Medicina
	(2, 321654987, '2018-06-20', 'Universidad Nacional'),
	
	-- Fede Perez (963852741) estudió Derecho
	(3, 963852741, '2021-11-10', 'Universidad de los Andes'),
	
	-- Pepito Perez (987654321) estudió Educación
	(4, 987654321, '2019-05-18', 'Universidad Pedagógica'),
	
	-- Pepita Juarez (147258369) estudió Contabilidad (aunque es joven, podría ser un caso especial)
	(5, 147258369, '2023-07-25', 'Universidad Minuto de Dios'),
	
	-- Algunos estudios adicionales (personas con múltiples carreras)
	-- Pepe también estudió Contabilidad
	(5, 123456789, '2022-03-12', 'Universidad Externado'),
	
	-- Pepa también estudió Derecho
	(3, 321654987, '2016-09-30', 'Universidad Javeriana'),
	
	-- Fede también estudió Ingeniería
	(1, 963852741, '2023-01-20', 'Universidad Nacional');