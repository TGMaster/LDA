/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.controller;

import lda.ml.Train;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import springboot.model.RequestModel;
import springboot.util.APIStatus;
import springboot.util.ApplicationException;
import springboot.util.Constant;
import springboot.util.RestAPIResponse;

import java.util.List;

/**
 * @author TGMaster
 */
@RestController
@RequestMapping(Constant.MODEL_API)
public class Controller extends BasedAPI {

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> trainModel(
            @RequestBody RequestModel requestModel
    ) {
        isValidModel(requestModel);
        int K = Integer.parseInt(requestModel.getK());
        int iter = Integer.parseInt(requestModel.getIteration());
        double train = Double.parseDouble(requestModel.getTraining())/100.0;

        List<String> json = Train.train(K, iter, train);
        return responseUtil.successResponse(json);
    }

    private void isValidModel(RequestModel requestModel) {
        if (requestModel.getTraining() == null || requestModel.getTraining().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_TRAINING);
        }
        if (requestModel.getIteration() == null || requestModel.getIteration().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_TOPIC);
        }
        if (requestModel.getK() == null || requestModel.getK().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_ITERATION);
        }
        if (requestModel.getOptimizer() == null || requestModel.getOptimizer().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_OPTIMIZER);
        }
    }
}
