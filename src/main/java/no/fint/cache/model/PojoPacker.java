package no.fint.cache.model;

public class PojoPacker implements Packer{

    @Override
    public byte[] pack(Object o) {
        return new byte[0];
    }

    @Override
    public Object unpack(byte[] b) {
        return null;
    }
}
