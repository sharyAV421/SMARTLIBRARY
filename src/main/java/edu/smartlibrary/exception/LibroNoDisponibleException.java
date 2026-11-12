package edu.smartlibrary.exception;

public class LibroNoDisponibleException extends RuntimeException {
    public LibroNoDisponibleException(Long libroId) { super("El libro " + libroId + " no está disponible"); }
}
