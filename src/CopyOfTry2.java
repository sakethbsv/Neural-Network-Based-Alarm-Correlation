	/**
	 * Copyright 2010 Neuroph Project http://neuroph.sourceforge.net
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *    http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */


	import java.util.Arrays;

	import org.neuroph.core.NeuralNetwork;
	import org.neuroph.core.learning.SupervisedTrainingElement;
	import org.neuroph.core.learning.TrainingSet;
	import org.neuroph.nnet.MultiLayerPerceptron;
	import org.neuroph.nnet.learning.MomentumBackpropagation;
	import org.neuroph.util.TransferFunctionType;

	/**
	 * This sample shows how to create, train, save and load simple Multi Layer Perceptron for the XOR problem.
	 * This sample shows basics of Neuroph API.
	 * @author Zoran Sevarac <sevarac@gmail.com>
	 */
	public class CopyOfTry2 {
	    /**
	     * Runs this sample
	     */
	    public static void main(String[] args) {
	    	
	        String path = "C:\\Users\\sbsv\\Desktop\\neural-input.txt";
	    	
	        TrainingSet trainingSet = TrainingSet.createFromFile(path, 8, 4, " ");
	        //public static TrainingSet createFromFile(java.lang.String filePath, int inputsCount, int outputsCount, java.lang.String delimiter)
	        
	        
	        // create multi layer perceptron
	        MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 8, 6, 4);

	        // enable batch if using MomentumBackpropagation
	        if( myMlPerceptron.getLearningRule() instanceof MomentumBackpropagation )
	        	((MomentumBackpropagation)myMlPerceptron.getLearningRule()).setBatchMode(true);

	        // learn the training set
	        System.out.println("Training neural network...");
	        myMlPerceptron.learn(trainingSet);

	        // test perceptron
	        System.out.println("Testing trained neural network");
	        //testNeuralNetwork(myMlPerceptron, trainingSet);

	        // save trained neural network
	        myMlPerceptron.save("myMlPerceptron.nnet");

	        // load saved neural network
	        NeuralNetwork loadedMlPerceptron = NeuralNetwork.load("myMlPerceptron.nnet");

	        // test loaded neural network
	        System.out.println("Testing loaded neural network");
	        testNeuralNetwork(loadedMlPerceptron, trainingSet);
	    }

	    /**
	     * Prints network output for the each element from the specified training set.
	     * @param neuralNet neural network
	     * @param trainingSet training set
	     */
	    public static void testNeuralNetwork(NeuralNetwork neuralNet, TrainingSet<SupervisedTrainingElement> trainingSet) {

	    	double[] inputArray = new double[8];
	    	
	    	for(int j = 0; j<inputArray.length; j++){
	    			
	    			inputArray[j] = 0;
	    	}
	    		
	    	inputArray[0] = 1;
	    	//System.out.println(Arrays.toString(inputArray));
	    	   neuralNet.setInput(inputArray);
	            neuralNet.calculate();
	            double[] networkOutput = neuralNet.getOutput();

	            System.out.print("Input: " + Arrays.toString( inputArray ) );
	            System.out.println(" Output: " + Arrays.toString( networkOutput) );

	            
	     /*   for(SupervisedTrainingElement trainingElement : trainingSet.elements()) {
	            neuralNet.setInput(trainingElement.getInput());
	            neuralNet.calculate();
	            double[] networkOutput = neuralNet.getOutput();

	            System.out.print("Input: " + Arrays.toString( trainingElement.getInput() ) );
	            System.out.println(" Output: " + Arrays.toString( networkOutput) );
	        }
	       */
	    }

	}
