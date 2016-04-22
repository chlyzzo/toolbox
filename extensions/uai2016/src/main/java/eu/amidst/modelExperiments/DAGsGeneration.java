/*
 *
 *
 *    Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 *    See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use
 *    this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under the License is
 *    distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and limitations under the License.
 *
 *
 */

package eu.amidst.modelExperiments;

import eu.amidst.core.datastream.Attribute;
import eu.amidst.core.datastream.Attributes;
import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.variables.Variable;
import eu.amidst.core.variables.Variables;
import eu.amidst.flinklink.core.data.DataFlink;
import eu.amidst.flinklink.core.io.DataFlinkWriter;
import eu.amidst.flinklink.core.utils.BayesianNetworkSampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by andresmasegosa on 17/2/16.
 */
public class DAGsGeneration {

    public static DAG getUAIMultiLocalGlobalDAG(Attributes attributes, int nstates) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define a local hidden variable.
        List<Variable> localHiddenVars = new ArrayList<>();
        List<Attribute> attributesList = attributes.getListOfNonSpecialAttributes();
        for (Attribute attribute : attributesList) {
            if (attribute.getName().compareTo("Default")==0)
                continue;
            localHiddenVars.add(variables.newMultionomialVariable("LocalHidden_"+attribute.getName(),nstates));
        }

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newMultionomialVariable("GlobalHidden",nstates);

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                //.filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                .forEach(w -> w.addParent(globalHiddenVar));

        // Link the local hidden as parent of all predictive attributes

        for (Attribute attribute : attributesList) {
            if (attribute.getName().compareTo("Default")==0)
                continue;

            dag.getParentSet(variables.getVariableByName(attribute.getName())).addParent(variables.getVariableByName("LocalHidden_"+attribute.getName()));
        }

        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getIDAMultiLocalGlobalDAG(Attributes attributes, int nlocals) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define a local hidden variable.
        List<Variable> localHiddenVars = new ArrayList<>();
        for (int i = 0; i < nlocals; i++) {
            localHiddenVars.add(variables.newGaussianVariable("LocalHidden_"+i));
        }

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newGaussianVariable("GlobalHidden");

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                .forEach(w -> w.addParent(globalHiddenVar));

        // Link the local hidden as parent of all predictive attributes
        for (Variable localHiddenVar : localHiddenVars) {
            dag.getParentSets()
                    .stream()
                    .filter(w -> w.getMainVar() != classVar)
                    .filter(w -> w.getMainVar() != globalHiddenVar)
                    .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                    .forEach(w -> w.addParent(localHiddenVar));
        }


        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getIDAMultiLocalGaussianDAG(Attributes attributes, int nlocals) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define a local hidden variable.
        List<Variable> localHiddenVars = new ArrayList<>();
        for (int i = 0; i < nlocals; i++) {
            localHiddenVars.add(variables.newGaussianVariable("LocalHidden_"+i));
        }


        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                .forEach(w -> w.addParent(classVar));

        // Link the local hidden as parent of all predictive attributes
        for (Variable localHiddenVar : localHiddenVars) {
            dag.getParentSets()
                    .stream()
                    .filter(w -> w.getMainVar() != classVar)
                    .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                    .forEach(w -> w.addParent(localHiddenVar));
        }


        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getIDAMultinomialMultiLocalGaussianDAG(Attributes attributes, int nstates, int nlocals) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define a local hidden variable.
        List<Variable> localHiddenVars = new ArrayList<>();
        for (int i = 0; i < nlocals; i++) {
            localHiddenVars.add(variables.newGaussianVariable("LocalHidden_"+i));
        }

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newMultionomialVariable("MultinomialHidden",nstates);

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .forEach(w -> w.addParent(globalHiddenVar));

        // Link the local hidden as parent of all predictive attributes
        for (Variable localHiddenVar : localHiddenVars) {
            dag.getParentSets()
                    .stream()
                    .filter(w -> w.getMainVar() != classVar)
                    .filter(w -> w.getMainVar() != globalHiddenVar)
                    .filter(w -> !w.getMainVar().getName().startsWith("Local"))
                    .forEach(w -> w.addParent(localHiddenVar));
        }


        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getIDALocalGlobalDAG(Attributes attributes) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define a local hidden variable.
        Variable localHiddenVar = variables.newGaussianVariable("LocalHidden");

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newGaussianVariable("GlobalHidden");

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> w.getMainVar() != localHiddenVar)
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> w.getMainVar() != localHiddenVar)
                .forEach(w -> w.addParent(globalHiddenVar));

        // Link the local hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> w.getMainVar() != localHiddenVar)
                .forEach(w -> w.addParent(localHiddenVar));


        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }


    public static DAG getIDAGlobalMultinomialDAG(Attributes attributes, int states) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newMultionomialVariable("GlobalHidden",states);

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .forEach(w -> w.addParent(globalHiddenVar));

        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getIDAGlobalDAG(Attributes attributes) {
        // Create a Variables object from the attributes of the input data stream.
        Variables variables = new Variables(attributes);

        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");

        // Define the global hidden variable.
        Variable globalHiddenVar = variables.newGaussianVariable("GlobalHidden");

        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .forEach(w -> w.addParent(classVar));

        // Link the global hidden as parent of all predictive attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .forEach(w -> w.addParent(globalHiddenVar));

        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static DAG getConnectedNBDAG(int n) {
        // Create a Variables object from the attributes of the input data stream.

        Variables variables = new Variables();

        variables.newMultionomialVariable("Default", 2);

        for (int i = 0; i < n; i++) {
            variables.newGaussianVariable("G_" + i);
        }


        // Define the class variable.
        Variable classVar = variables.getVariableByName("Default");


        // Create an empty DAG object with the defined variables.
        DAG dag = new DAG(variables);

        // Link the class as parent of all attributes
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .forEach(w -> w.addParent(classVar));


        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                dag.getParentSet(variables.getVariableByName("G_" + i)).addParent(variables.getVariableByName("G_" + j));
            }
        }

        // Show the new dynamic DAG structure
        System.out.println(dag.toString());

        return dag;
    }

    public static void generateData(int nVars, int nsamples, int batchsize) throws Exception {
        DAG dag = getConnectedNBDAG(nVars);
        BayesianNetwork bn = new BayesianNetwork(dag);
        bn.randomInitialization(new Random(0));

        System.out.println(bn.toString());

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setBatchSize(batchsize);

        DataFlink<DataInstance> data = sampler.sampleToDataFlink(nsamples);

        System.out.println(getUAIMultiLocalGlobalDAG(data.getAttributes(),2));

        DataFlinkWriter.writeDataToARFFFolder(data, "./datasets/dataFlink/data.arff");

    }


    public static void main(String[] args) throws Exception {
        int nVars = 0;
        int dataSetSize=4000;
        int windowSize = 1000;
        generateData(nVars,dataSetSize, windowSize);


    }

}