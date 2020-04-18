import os
import re
import sys
import numpy as np
from matplotlib import pyplot as plt
import cv2
from matplotlib.colors import LogNorm
import time
from scipy import sparse
from scipy.sparse import csr_matrix
	
def dft(x):
	x = np.asarray(x, dtype=complex)
	N = x.shape[0]
	n = np.arange(N)
	k = n.reshape((N, 1))
	T = np.exp(-2j * np.pi * k * n / N)
	return np.dot(T, x)

def invdft(x):
    x = np.asarray(x, dtype=complex)
    N = x.shape[0]
    n = np.arange(N)
    k = n.reshape((N,1))
    T = np.exp(2j * np.pi * k * n / N)
    return ((np.dot(T, x))/N)


def FFT(x):
	x = np.asarray(x, dtype=complex)
	N = x.shape[0]

	if N % 2 > 0:
		raise ValueError("size of x must be a power of 2")
	elif N <= 32:
		return dft(x)
	else:
		X_even = FFT(x[::2])
		X_odd = FFT(x[1::2])
		factor = np.exp(-2j * np.pi * np.arange(N) / N)
		return np.concatenate([X_even + factor[:int(N/2)] * X_odd, X_even + factor[int(N/2):] * X_odd])

def invFFT(x):
    x = np.asarray(x, dtype=complex)
    N = x.shape[0]

    if N % 2 > 0:  
        raise ValueError("size of x must be a power of 2")
    elif N <= 32:
        return invdft(x)
    else:
        X_even = invFFT(x[::2])
        X_odd = invFFT(x[1::2])
        factor = np.exp(2j * np.pi * np.arange(N) / N)
        return np.concatenate([X_even + factor[:int(N/2)] * X_odd, X_even + factor[int(N/2):] * X_odd])

def twoDFFT(x):
	x = np.asarray(x, dtype=complex)
	
	N = x.shape[0] #rows
	M = x.shape[1] #columns
	
	for i in range(N): #for each row
		x[i] = FFT(x[i])
	for a in range(M): #for each column
		x[:,a] = FFT(x[:,a])
	return x

def twoDFTNaive(x):
	x = np.asarray(x, dtype=complex)
	
	N = x.shape[0] #rows
	M = x.shape[1] #columns
	
	for i in range(N): #for each row
		x[i] = dft(x[i])
	for a in range(M): #for each column
		x[:,a] = dft(x[:,a])
	return x

def invtwoDFFT(x):
	x = np.asarray(x, dtype=complex)
	N = x.shape[0] #rows
	M = x.shape[1] #columns

	for a in range(M): #for each column
		x[:,a] = invFFT(x[:,a])
	for i in range(N): #for each row
		x[i] = invFFT(x[i])	

	return x

def compression(twodfft, perc):
	twodfftcopy = np.copy(twodfft)
	total = twodfftcopy.shape[0] * twodfftcopy.shape[1]
	zeroes = (int) (total * perc / 100)
	nonzeroes = total - zeroes
	
	highfreq = np.copy(twodfftcopy)
	r, c = twodfftcopy.shape
	
	#choosing coefficient to delimit low/high freqs in rectangles
	totalnonzeroes = nonzeroes/5
	coeff = (total**0.5)/(totalnonzeroes**0.5)
	zeroesR = int(round(r/coeff))
	zeroesC = int(round(c/coeff))

	#everything that is high freq is set to 0
	twodfftcopy[zeroesR:r-zeroesR] = 0
	twodfftcopy[:, zeroesC:c-zeroesC] = 0
	
	
	#everything that is low freq (not the middle) is set to 0
	rmid = int(r/2)
	cmid = int(c/2)
	zerosSqHalfR = int(zeroesR/2)
	zerosSqHalfC = int(zeroesC/2)
	highfreq[0:rmid-zerosSqHalfR] = 0
	highfreq[rmid+zerosSqHalfR:r] = 0
	highfreq[:,0:cmid-zerosSqHalfC] = 0
	highfreq[:,cmid+zerosSqHalfC:c] = 0
	
	#add high freq and low freq matrices
	compressed = np.add(twodfftcopy, highfreq)
	count = np.count_nonzero(compressed)
	print("Information for image compression at " +str(perc) + "%")
	print("Non-zero entries we are using: " + str(count))
	
	name = "sparse_matrix_" + str(perc) + ".npz"
	print("Saving sparse matrix file " + name)
	array = csr_matrix(compressed)
	#sparse.save_csr(name, sparse_m)
	np.savez(name, data=array.data, indices=array.indices, indptr=array.indptr, shape=array.shape)
			 
	#replaces the zeroes highest frequencies by 0
	return (np.real(invtwoDFFT(compressed)))

def mode_1(image):
	print("Mode 1 chosen, please wait....")
	im = cv2.imread(image, cv2.IMREAD_GRAYSCALE)
	width = im.shape[1]
	height = im.shape[0]
	# calculating new sizes to be powers of 2
	while np.log2(width)%1 != 0:
		width = width+1		
	while np.log2(height)%1 != 0:
		height = height+1
	#padding image with zeros
	resized = np.pad(im, ((0,height-im.shape[0]),(0,width-im.shape[1])), mode='constant')
	
	correctfft = np.fft.fft2(resized)
	twodfft = twoDFFT(resized)
	
	# A logarithmic colormap
	fig, axs = plt.subplots(1, 2)
	axs[0].imshow(resized, cmap='gray')
	axs[0].set_title('Resized Image')

	im2 = axs[1].imshow(np.abs(twodfft), norm=LogNorm(vmin=5))
	axs[1].set_title('Our 2D Fast Fourier Transform')
	fig.colorbar(im2, ax=axs[1])
	
	# A logarithmic colormap
	fig, axs = plt.subplots(1, 2)
	axs[0].imshow(resized, cmap='gray')
	axs[0].set_title('Resized Image')

	im2 = axs[1].imshow(np.abs(correctfft), norm=LogNorm(vmin=5))
	axs[1].set_title('NP Library 2D Fourier Transform')
	fig.colorbar(im2, ax=axs[1])

	plt.show()
	cv2.waitKey(0)

def mode_2(image):
	print("Mode 2 chosen, please wait....")
	im = cv2.imread(image, cv2.IMREAD_GRAYSCALE)
	width = im.shape[1]
	height = im.shape[0]
	# calculating new sizes to be powers of 2
	while np.log2(width)%1 != 0:
		width = width+1		
	while np.log2(height)%1 != 0:
		height = height+1
	#padding image with zeros
	resized = np.pad(im, ((0,height-im.shape[0]),(0,width-im.shape[1])), mode='constant')


	twodfft = (twoDFFT(resized))
	r, c = twodfft.shape
	#choosing coefficient to delimit low/high freqs
	arbCoeff = 0.3
	#getting values where low frequencies start
	xfreq = int((c/2) * arbCoeff)
	yfreq = int((r/2) * arbCoeff)
	#everything that is high freq is set to 0
	twodfft[yfreq:r-yfreq] = 0
	twodfft[:, xfreq:c-xfreq] = 0
	
	count = np.count_nonzero(twodfft)
	print("Non-zero entries we are using: " + str(count))
	print("Fraction they represent on original fourrier coefficients: " + str(count / (twodfft.shape[1] * twodfft.shape[0])))
	
	i2dfft = np.real(invtwoDFFT(twodfft))

	# A logarithmic colormap
	fig, axs = plt.subplots(1, 2)
	axs[0].imshow(resized, cmap='gray')
	axs[0].set_title('Resized Image')

	axs[1].imshow(i2dfft, cmap='gray')
	axs[1].set_title('Denoised Image (Our code)')


	plt.show()
	cv2.waitKey(0)


def mode_3(image):
	print("Mode 3 chosen, please wait....")
	im = cv2.imread(image, cv2.IMREAD_GRAYSCALE)
	width = im.shape[1]
	height = im.shape[0]
	# calculating new sizes to be powers of 2
	while np.log2(width)%1 != 0:
		width = width+1		
	while np.log2(height)%1 != 0:
		height = height+1
	#padding image with zeros
	resized = np.pad(im, ((0,height-im.shape[0]),(0,width-im.shape[1])), mode='constant')

	twodfft = twoDFFT(resized)
	
	comp0 = compression(twodfft, 0)
	comp1 = compression(twodfft, 10)
	comp2 = compression(twodfft, 25)
	comp3 = compression(twodfft, 50)
	comp4 = compression(twodfft, 75)
	comp5 = compression(twodfft, 95)

	# A logarithmic colormap
	fig, axs = plt.subplots(2, 3)
	axs[0][0].imshow(resized, cmap='gray')
	axs[0][0].set_title('0% Compression')

	axs[0][1].imshow(comp1, cmap='gray')
	axs[0][1].set_title('10% Compression')

	axs[0][2].imshow(comp2, cmap='gray')
	axs[0][2].set_title('25% Compression')

	axs[1][0].imshow(comp3, cmap='gray')
	axs[1][0].set_title('50% Compression')

	axs[1][1].imshow(comp4, cmap='gray')
	axs[1][1].set_title('75% Compression')

	axs[1][2].imshow(resized, cmap='gray')
	axs[1][2].set_title('95% Compression')


	plt.show()
	cv2.waitKey(0)
    

def mode_4(image):
	print("Mode 4 chosen, please wait.... it might take a while")
	power = [5,6,7,8,9]
	runtimeN = [0,0,0,0,0]
	standdevN = [0,0,0,0,0] 
	runtimeF = [0,0,0,0,0]
	standdevF = [0,0,0,0,0] 
	i=0
	
	while i < len(power):
		j=0
		timeN = [0,0,0,0,0,0,0,0] 
		timeF = [0,0,0,0,0,0,0,0] 

		#run it 10 times
		while j < len(timeN):
			x = np.random.random((2**power[i],2**power[i]))
			# naive run
			startN = int(round(time.time() * 1000))
			naive = twoDFTNaive(x) #replace by our own algorithm
			endN = int(round(time.time() * 1000))
			timeN[j] = endN-startN
			
			# fft run
			startF = int(round(time.time() * 1000))
			fft = twoDFFT(x) #replace by our own algorithm
			endF = int(round(time.time() * 1000))
			timeF[j] = endF-startF
			
			j=j+1
		#compute run times
		runtimeN[i]= np.mean(timeN)
		standdevN[i] = np.std(timeN)*2
		runtimeF[i]= np.mean(timeF)
		standdevF[i] = np.std(timeF)*2
		print(runtimeF[i])
		print(standdevF[i])
		
		i=i+1
	
	#plot graph, x = power, y = runtime, 2 lines = naive and fft
	plt.errorbar(power, runtimeN, yerr=standdevN, marker='o', markerfacecolor='blue', markersize=5, color='skyblue', linewidth=2, label='naive')
	plt.errorbar(power, runtimeF, yerr=standdevF, marker='o', markerfacecolor='pink', markersize=4, color='grey', linewidth=2, label='FFT', capsize=4, ecolor = 'black', elinewidth=2)
	plt.ylabel('Runtime (milliseconds)')
	plt.xlabel('Power')
	plt.title('Runtime of Naive vs FFT methods')
	plt.legend()
	plt.show()
	cv2.waitKey(0)
	

if __name__ == '__main__':
	n = sys.argv
	size = len(sys.argv)
	if(n[0] != "fft.py"):
		print("Command line synthax must have the form: python fft.py [-m mode] [-i image]")

	image = "moonlanding.png"
	mode = 1
	
	modeset = False
	imageset = False
	
	i = 1
	while i < size:
		if n[i] == "-i" and imageset == False:
			image = n[i+1]
			imageset = True
		elif n[i] == "-m" and modeset == False:
			mode = int(n[i+1])
			modeset = True
		else:
			print("Arguments cannot be parsed")
			exit()
		i = i+2

	if mode == 1:
		mode_1(image)
	elif mode == 2:
		mode_2(image)
	elif mode == 3:
		mode_3(image)
	elif mode == 4:
		mode_4(image)
	else:
		print("Mode has to be between 1 and 4")
		exit()	
