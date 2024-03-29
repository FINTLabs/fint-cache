package no.fint.cache.model;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class PackerFactory {
    private PackerFactory() {

    }

    public static Packer create(String type) {
        switch (type.toUpperCase()) {
            case "DEFLATE":
                return new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
            default:
                return new SerializationPacker();
        }

    }
}
