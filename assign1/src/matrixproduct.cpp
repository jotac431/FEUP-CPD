#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
//#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

void PrintMatrix(double *matrix, int numRows, int numCols) {
	cout << "[";
	for (int row = 0; row < numRows; ++row) {
		if (row > 0) cout << "]\n ";
		cout << "[";
		for (int col = 0; col < numCols; ++col) {
			if (col > 0) cout << ", ";
			cout << matrix[row * numCols + col];
		}
	}
	cout << "]]" << endl;
}

void OnMult(double *matA, double *matB, double *matC, int numRows) {
	int i, j, k;
	double temp;
	for (i = 0; i < numRows; ++i) { // for every row in C
		for(j = 0; j < numRows; ++j) { // for every col in C
			temp = 0; // accumulator
			for(k = 0; k < numRows; ++k) { // for every cell in a row from A or every cell in a col from B
				temp += matA[i * numRows + k] * matB[k * numRows + j]; // dot product
			}
			matC[i * numRows + j] = temp;
		}
	}
}

void OnMultLine(double *matA, double *matB, double *matC, int numRows) {
	int i, j, k;
	double temp;
	for (k = 0; k < numRows; ++k) { // for every col in A
		for (i = 0; i < numRows; ++i) { // for every row in A
			temp = matA[i * numRows + k]; // get cell from A, Aik
			for (j = 0; j < numRows; ++j) { // for every cell j in row from both B and C
				matC[i * numRows + j] += temp * matB[k * numRows + j]; // scalar product of the row from B with Aik, gets added onto the row from C
			}
		}
	}
}

void OnMultBlock(double *matA, double *matB, double *matC, int numRows, int blockSize) {
	int ii, jj, kk, i, j, k;
	for (ii = 0; ii < numRows; ii += blockSize) { // for every block in a row
		for (jj = 0; jj < numRows; jj += blockSize) { // for every block in a col
			for (kk = 0; kk < numRows; kk += blockSize) { // improve temporal locality
				for (i = ii; i < ii + blockSize; ++i) { // for every row in a block
					for (j = jj; j < jj + blockSize; ++j) { // for every col in a block
						for (k = kk; k < kk + blockSize; ++k) { // iterate over temporally close cells only
							matC[i * numRows + j] += matA[i * numRows + k] * matB[k * numRows + j]; // accumulate those cells onto Cij
						}
					}
				}
			}
		}
	}
}

void MatrixMult(int multAlgorithm, int numRows, int blockSize = 0) {
	SYSTEMTIME Time1, Time2;
	
	char st[100];

	// allocate square matrices A, B, and C (result)
	double *matA, *matB, *matC;
    matA = (double *) malloc((numRows * numRows) * sizeof(double));
	matB = (double *) malloc((numRows * numRows) * sizeof(double));
	matC = (double *) malloc((numRows * numRows) * sizeof(double));

	// initialize matrices A, B, and C
	for (int i = 0; i < numRows; ++i) {
		for (int j = 0; j < numRows; ++j) {
			matA[i * numRows + j] = 1.0;
			matB[i * numRows + j] = i + 1.0;
			matC[i * numRows + j] = 0.0;
		}
	}

    Time1 = clock();

	// multiply matrices using chosen method
	if (multAlgorithm == 1) OnMult(matA, matB, matC, numRows);
	if (multAlgorithm == 2) OnMultLine(matA, matB, matC, numRows);
	if (multAlgorithm == 3) OnMultBlock(matA, matB, matC, numRows, blockSize);

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 2 rows and 10 columns of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	PrintMatrix(matC, min(2, numRows), min(10, numRows));
	cout << endl;

    free(matA);
    free(matB);
    free(matC);	
}

/*
void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}
*/

int main (int argc, char *argv[]) {
	//int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	
/*
	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

*/
	int op, lin, blockSize;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;


		// Start counting
//		ret = PAPI_start(EventSet);
//		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		if (op >= 1 && op <= 3) {
			cout << "Dimensions: lins=cols ? ";
			cin >> lin;
			if (op == 3) {
				cout << "Block Size? ";
				cin >> blockSize;
				MatrixMult(op, lin, blockSize);
			} else MatrixMult(op, lin);
		}
/*
  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 
*/


	} while (op != 0);
	/*
	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;
	*/
}
