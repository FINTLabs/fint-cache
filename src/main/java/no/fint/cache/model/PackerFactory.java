package no.fint.cache.model;

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.jooq.lambda.Unchecked;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class PackerFactory {
    private PackerFactory() {
        
    }
    
    public static Packer create(String type) {
        switch (type.toUpperCase()) {
            case "LZMA":
                return new CompressingPacker(Unchecked.function(LZMACompressorOutputStream::new), Unchecked.function(LZMACompressorInputStream::new));
            case "DEFLATE2":
                return new CompressingPacker(Unchecked.function(DeflateCompressorOutputStream::new), Unchecked.function(DeflateCompressorInputStream::new));
            case "DEFLATE":
                return new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
            case "SNAPPY":
                return new CompressingPacker(Unchecked.function(FramedSnappyCompressorOutputStream::new), Unchecked.function(FramedSnappyCompressorInputStream::new));
            case "ZSTD":
                return new CompressingPacker(Unchecked.function(ZstdCompressorOutputStream::new), Unchecked.function(ZstdCompressorInputStream::new));
            case "LZ4":
                return new CompressingPacker(Unchecked.function(BlockLZ4CompressorOutputStream::new), Unchecked.function(BlockLZ4CompressorInputStream::new));
            default:
                return new SerializationPacker();
        }

    }
}
