package il.co.fbc.sizeoff.mapper;

public interface MapperFromTo<From, To>{
    To map(From from);
}
