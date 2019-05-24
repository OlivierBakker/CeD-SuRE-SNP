package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.barcodefilereader.BarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.GenericBarcodeFileReader;

import java.io.IOException;

public class GenerateBarcodeComplexityCurve {


    private GenerateBarcodeComplexityCurveParameters params;
    private BarcodeFileReader barcodeFileReader;

    public GenerateBarcodeComplexityCurve(GenerateBarcodeComplexityCurveParameters params) throws IOException {

        this.params = params;
        this.barcodeFileReader = new GenericBarcodeFileReader(params.getOutputPrefix());

    }


    public void run() {

    }



}
