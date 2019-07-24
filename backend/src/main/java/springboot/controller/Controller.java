/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.controller;

import lda.ml.Preprocess;
import lda.ml.Test;
import lda.ml.Train;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import springboot.model.RequestLDAModel;
import springboot.model.RequestProcessModel;
import springboot.util.APIStatus;
import springboot.util.ApplicationException;
import springboot.util.Constant;
import springboot.util.RestAPIResponse;

import java.util.List;

/**
 * @author TGMaster
 */
@RestController
public class Controller extends BasedAPI {

    // TrainProcess
    @RequestMapping(value=Constant.LDA_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> trainModel(
            @RequestBody RequestLDAModel requestModel
    ) {
        isValidModel(requestModel);
        int K = Integer.parseInt(requestModel.getK());
        double alpha = Double.parseDouble(requestModel.getAlpha());
        double beta = Double.parseDouble(requestModel.getBeta());

        List<String> json = Train.train(K, alpha, beta);
        return responseUtil.successResponse(json);
    }


    // PreProcess
    @RequestMapping(value=Constant.PREPROCESS_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> preProcess(
            @RequestBody RequestProcessModel requestModel
    ) {
        isValidInput(requestModel);

        List<String> json = Preprocess.preprocess(requestModel.getFilename(), requestModel.getColumn(), null);
        return responseUtil.successResponse(json);
    }

    // View schema
    @RequestMapping(value=Constant.PREPROCESS_API ,method = RequestMethod.GET)
    public ResponseEntity<RestAPIResponse> previewDataset(
            @RequestParam(value = "dataset", required = true) String dataset
    ) {
        return responseUtil.successResponse(Preprocess.preview(dataset, null));
    }


    // Search
    @RequestMapping(value=Constant.TEST_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> search(
            @RequestBody String keyword
    ) {
        return responseUtil.successResponse(Test.Search(keyword));
    }


    // Utils
    private void isValidModel(RequestLDAModel requestModel) {
        if (requestModel.getAlpha() == null || requestModel.getAlpha().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_ALPHA);
        }
        if (requestModel.getBeta() == null || requestModel.getBeta().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_BETA);
        }
        if (requestModel.getK() == null || requestModel.getK().equals("")) {
            throw new ApplicationException(APIStatus.ERR_MODEL_MISSING_TOPIC);
        }
    }

    private void isValidInput(RequestProcessModel requestModel) {
        if (requestModel.getFilename() == null || requestModel.getFilename().equals("")) {
            throw new ApplicationException(APIStatus.ERR_PREPROCESS_MISSING_FILENAME);
        }
        if (requestModel.getColumn() == null || requestModel.getColumn().equals("")) {
            throw new ApplicationException(APIStatus.ERR_PREPROCESS_MISSING_COLUMN);
        }
    }
}
