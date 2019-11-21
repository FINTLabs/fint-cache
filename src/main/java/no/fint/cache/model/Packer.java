package no.fint.cache.model;

public interface Packer {
    byte[] pack(Object o);
    Object unpack(byte[] b);
}
