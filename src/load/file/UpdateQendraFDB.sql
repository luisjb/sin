SET TERM ^;

CREATE OR ALTER PROCEDURE merge_product(
 the_number INTEGER,
 the_seccion INTEGER,
 the_plu_name VARCHAR(18),
 the_codigo INTEGER,
 the_type SMALLINT,
 the_tare INTEGER,
 the_vencimiento SMALLINT,
 the_price FLOAT,
 the_price2 FLOAT,
 the_others BLOB SUB_TYPE 0,
 the_modifby VARCHAR(60),
 the_porc_agua DECIMAL,
 the_tn_activa SMALLINT,
 the_tn_desc VARCHAR(30),
 the_tn_cal_porc FLOAT,
 the_tn_carbohidratos FLOAT,
 the_tn_proteinas FLOAT,
 the_tn_grasas_tot FLOAT,
 the_tn_grasas_sat FLOAT,
 the_tn_grasas_trans FLOAT,
 the_tn_fibra FLOAT,
 the_tn_sodio FLOAT,
 the_origen INTEGER,
 the_conserv INTEGER,
 the_recing INTEGER,
 the_lote VARCHAR(13),
 the_ean_tipo SMALLINT,
 the_ean_cfg VARCHAR(12)
)
AS
BEGIN
 BEGIN
  INSERT INTO PLU(ID, ID_SECCION, DESCRIPCION, COD_LOCAL, TIPO_VENTA, TARA, VENCIMIENTO, PRECIO, PRECIO2, OTROS, ULTIMA_MODIF,
		  MODIFBY, IMPFLAG, PORC_AGUA, TN_ACTIVA, TN_DESC, TN_CAL_PORCION, TN_CARBOHIDRATOS, TN_PROTEINAS, TN_GRASAS_TOT,
		  TN_GRASAS_SAT, TN_GRASAS_TRANS, TN_FIBRA, TN_SODIO, ID_ORIGEN, ID_CONSERV, ID_RECING,
		  LOTE, EAN_TIPO, EAN_CFG)
VALUES(
 :the_codigo,
 :the_seccion,
 :the_plu_name,
 :the_number,
 :the_type,
 :the_tare,
 :the_vencimiento,
 :the_price,
 :the_price2,
 :the_others,
 CURRENT_TIMESTAMP,
 :the_modifby,
 1,
 :the_porc_agua,
 :the_tn_activa,
 :the_tn_desc,
 :the_tn_cal_porc,
 :the_tn_carbohidratos,
 :the_tn_proteinas,
 :the_tn_grasas_tot,
 :the_tn_grasas_sat,
 :the_tn_grasas_trans,
 :the_tn_fibra,
 :the_tn_sodio,
 :the_origen,
 :the_conserv,
 :the_recing,
 :the_lote,
 :the_ean_tipo,
 :the_ean_cfg
);

 WHEN ANY DO
  UPDATE PLU SET 
   ID_SECCION = :the_seccion,
   DESCRIPCION = :the_plu_name,
   COD_LOCAL = :the_number, 
   TIPO_VENTA = :the_type, 
   TARA = :the_tare,
   VENCIMIENTO = :the_vencimiento,
   PRECIO = :the_price,
   PRECIO2 = :the_price2,
   OTROS = :the_others,
   ULTIMA_MODIF = CURRENT_TIMESTAMP,
   MODIFBY = :the_modifby,
   IMPFLAG = 1,
   PORC_AGUA = :the_porc_agua,
   TN_ACTIVA = :the_tn_activa,
   TN_DESC = :the_tn_desc,
   TN_CAL_PORCION = :the_tn_cal_porc,
   TN_CARBOHIDRATOS = :the_tn_carbohidratos,
   TN_PROTEINAS = :the_tn_proteinas,
   TN_GRASAS_TOT = :the_tn_grasas_tot,
   TN_GRASAS_SAT = :the_tn_grasas_sat,
   TN_GRASAS_TRANS = :the_tn_grasas_trans,
   TN_FIBRA = :the_tn_fibra,
   TN_SODIO = :the_tn_sodio,
   ID_ORIGEN = :the_origen,
   ID_CONSERV = :the_conserv,
   ID_RECING = :the_recing,
   LOTE = :the_lote,
   EAN_TIPO = :the_ean_tipo,
   EAN_CFG = :the_ean_cfg
  WHERE ID = :the_codigo;
 END
END^

CREATE OR ALTER PROCEDURE merge_department(
 the_id INTEGER,
 the_nombre VARCHAR(18)
)
AS
BEGIN
 BEGIN
  INSERT INTO SECCIONES(ID, NOMBRE, IMPFLAG)
VALUES(
 :the_id,
 :the_nombre,
 1
);

 WHEN ANY DO
  UPDATE SECCIONES SET 
   NOMBRE = :the_nombre,
   IMPFLAG = 1
  WHERE ID = :the_id;
 END
END^

CREATE OR ALTER PROCEDURE merge_origen(
 the_id INTEGER,
 the_nombre VARCHAR(20),
 the_info VARCHAR(300)
)
AS
BEGIN
 BEGIN
  INSERT INTO ORIGENES(ID, ACTIVO, NOMBRE, INFO)
VALUES(
 :the_id,
 1, 
 :the_nombre,
 :the_info
);

 WHEN ANY DO
  UPDATE ORIGENES SET 
   NOMBRE = :the_nombre,
   ACTIVO = 1,
   INFO = :the_info
  WHERE ID = :the_id;
 END
END^

CREATE OR ALTER PROCEDURE merge_datos_conserv(
 the_id INTEGER,
 the_nombre VARCHAR(20),
 the_info VARCHAR(300)
)
AS
BEGIN
 BEGIN
  INSERT INTO DATOS_CONSERV(ID, ACTIVO, NOMBRE, INFO)
VALUES(
 :the_id,
 1, 
 :the_nombre,
 :the_info
);

 WHEN ANY DO
  UPDATE DATOS_CONSERV SET 
   NOMBRE = :the_nombre,
   ACTIVO = 1,
   INFO = :the_info
  WHERE ID = :the_id;
 END
END^

CREATE OR ALTER PROCEDURE merge_ingredientes(
 the_id INTEGER,
 the_nombre VARCHAR(20),
 the_info VARCHAR(300)
)
AS
BEGIN
 BEGIN
  INSERT INTO INGREDIENTES(ID, ACTIVO, NOMBRE, INFO)
VALUES(
 :the_id,
 1, 
 :the_nombre,
 :the_info
);

 WHEN ANY DO
  UPDATE INGREDIENTES SET 
   NOMBRE = :the_nombre,
   ACTIVO = 1,
   INFO = :the_info
  WHERE ID = :the_id;
 END
END^
