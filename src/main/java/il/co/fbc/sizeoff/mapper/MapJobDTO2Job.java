package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.controllers.dto.JobDTO;
import il.co.fbc.sizeoff.domain.entities.Job;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapJobDTO2Job extends MapperFromTo<JobDTO, Job>{
}
