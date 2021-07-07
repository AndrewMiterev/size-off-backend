package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.common.dto.TabulaDTO;
import il.co.fbc.sizeoff.services.bo.TabulaBo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.nio.file.Paths;

@Mapper(componentModel = "spring")
public interface MapTabulaDTOtoTabulaBo extends MapperFromTo<TabulaDTO, TabulaBo> {
    @Mapping(target = "installationID", expression = "java(normalizeID(dto.getInstallationID(),\"#\"))")
    @Mapping(target = "path", expression = "java(normalizePath(dto.getName(), dto.getPath()))")
    TabulaBo map(TabulaDTO dto);

    default String normalizeID(String id, String ch) {
        return id == null || id.length() == 0 ? "" : id.contains(ch) ? id : String.format("%s%s", id, ch);
    }

    default String normalizePath(String name, String path) {
        return Paths.get(String.format("\\\\%s\\%s\\..", name, path.replace(":", "$"))).normalize().toString();
    }
}
