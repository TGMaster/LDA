package springboot.controller;


import org.springframework.beans.factory.annotation.Autowired;
import springboot.util.ResponseUtil;

public abstract class BasedAPI {

    @Autowired
    protected ResponseUtil responseUtil;

}
