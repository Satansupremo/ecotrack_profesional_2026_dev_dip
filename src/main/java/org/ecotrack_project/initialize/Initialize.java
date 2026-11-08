package org.ecotrack_project.initialize;

public class Initialize {

    public static int TAMANIO_PAGINA;
    public static int CANTIDAD_INTENTO_PAGO_INICIAL;
    public static int CANTIDAD_INTENTO_PAGO_FINAL;
    public static String MENSAJE_CONFIRMACION_PAGO_INICIAL;
    public static String MENSAJE_RECHAZO_PAGO_INICIAL;
    static{
        TAMANIO_PAGINA = 7;
        CANTIDAD_INTENTO_PAGO_INICIAL = 3;
        CANTIDAD_INTENTO_PAGO_FINAL = 10;
        MENSAJE_CONFIRMACION_PAGO_INICIAL = "Su pago inicial ha sido confirmado correctamente. "
                + "El coordinador se pondrá en contacto con usted mediante "
                + "su número de celular o correo electrónico para coordinar "
                + "la verificación de la estimación del costo total del servicio. "
                + "Importante: El monto de su pago inicial será descontado "
                + "del costo total, por lo que no tendrá que pagarlo nuevamente.";
        MENSAJE_RECHAZO_PAGO_INICIAL = "Su solicitud ha sido rechazada definitivamente porque alcanzó "
                + "el límite máximo de tres intentos de pago inicial. "
                + "Motivo del último rechazo: ";
    }
}

// TAMANIO PAGINA: define la cantidad de registros mostrados por pagina
// CANTIDAD INTENTO PAGO INICIAL: define la cantidad de intentos que tiene el usuario para registrar su pago inicial. una vez pasado, se bloquea.