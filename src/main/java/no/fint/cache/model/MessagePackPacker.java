package no.fint.cache.model;

import lombok.SneakyThrows;
import org.msgpack.MessagePack;

public class MessagePackPacker implements Packer {
    private final MessagePack messagePack;

    public MessagePackPacker() {
        messagePack = new MessagePack();
    }

    @Override
    @SneakyThrows
    public byte[] pack(Object o) {
        return messagePack.write(o);
    }

    @Override
    @SneakyThrows
    public Object unpack(byte[] b) {
        return messagePack.read(b);
    }
}
