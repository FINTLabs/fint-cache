package no.fint.cache.model;

import lombok.SneakyThrows;

import java.io.*;
import java.util.function.Function;

public class CompressingPacker implements Packer {

    private final Function<OutputStream, OutputStream> outputStreamConstructor;
    private final Function<InputStream, InputStream> inputStreamConstructor;

    public CompressingPacker(Function<OutputStream, OutputStream> outputStreamConstructor, Function<InputStream, InputStream> inputStreamConstructor) {
        this.outputStreamConstructor = outputStreamConstructor;
        this.inputStreamConstructor = inputStreamConstructor;
    }

    @Override
    @SneakyThrows
    public byte[] pack(Object o) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oout = new ObjectOutputStream(outputStreamConstructor.apply(out))) {
            oout.writeObject(o);
        }
        return out.toByteArray();
    }

    @Override
    @SneakyThrows
    public Object unpack(byte[] b) {
        try (ObjectInputStream oin = new ObjectInputStream(inputStreamConstructor.apply(new ByteArrayInputStream(b)))) {
            return oin.readObject();
        }
    }
}
