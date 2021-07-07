package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.controllers.dto.JobDTO;
import il.co.fbc.sizeoff.domain.entities.Job;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MapJob2JobDTO extends MapperFromTo<Job, JobDTO>{
    List<JobDTO> map (List<Job> jobs);
}
