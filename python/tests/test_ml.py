from __future__ import unicode_literals
import unittest

import pandas as pd
from py4j.java_gateway import Py4JJavaError

import test_base
from ddf import ml


class TestMl(test_base.BaseTest):
    """
    Test ML functions
    """

    def testKmeans(self):
        model = ml.kmeans(self.mtcars, 2, 5, 10)
        self.assertIsInstance(model, ml.KMeansModel)
        self.assertIsInstance(model.centers, pd.DataFrame)
        self.assertEqual(len(model.centers), 2)
        self.assertItemsEqual(model.centers.columns.tolist(), self.mtcars.colnames)

        self.assertIsInstance(model.predict(range(0, self.mtcars.ncol)), float)
        with self.assertRaises(Py4JJavaError):
            model.predict([0, 1, 2])

    def testLinearRegression(self):
        model = ml.linear_regression_gd(self.mtcars, 0.1, 0.1, 10)
        self.assertIsInstance(model, ml.LinearRegressionModel)
        self.assertIsInstance(model.weights, pd.DataFrame)
        self.assertEqual(len(model.weights), 1)
        self.assertEqual(len(model.weights.columns), self.mtcars.ncol)

        self.assertIsInstance(model.predict(range(0, self.mtcars.ncol - 1)), float)
        with self.assertRaises(Py4JJavaError):
            model.predict([0, 1, 2])

    def testLogisticRegression(self):
        model = ml.logistic_regression_gd(self.mtcars, 0.1, 10)
        self.assertIsInstance(model, ml.LogisticRegressionModel)
        self.assertIsInstance(model.weights, pd.DataFrame)
        self.assertEqual(len(model.weights), 1)
        self.assertEqual(len(model.weights.columns), self.mtcars.ncol)

        self.assertIsInstance(model.predict(range(0, self.mtcars.ncol - 1)), float)
        with self.assertRaises(Py4JJavaError):
            model.predict([0, 1, 2])

if __name__ == '__main__':
    unittest.main()
