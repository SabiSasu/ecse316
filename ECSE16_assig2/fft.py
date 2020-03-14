import os
import re
import sys
import numpy as np
from matplotlib import pyplot as plt
import cv2
from matplotlib.colors import LogNorm
import time
	
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
    return ((np.dot(T, x)) / N)

def twoDDFT(x):
    x = np.asarray(x, dtype=complex)
    N = x.shape[0] #rows
    M = x.shape[1] #columns
    n = np.arange(N) #array from 0 to N-1
    m = np.arange(M) #.reshape(M, 1) #array from 0 to M-1
    k = m.reshape((M, 1))
    l = n.reshape((1, N))
    T = np.exp(-2j * np.pi * k * m / M) #inner
    U = np.exp(-2j * np.pi * l * n / N) #outer

    #W = np.exp(-2j * np.pi * m / M) #inner
    #Z = np.exp(-2j * np.pi * n / N) #outer
    #coeffs = np.zeros((N, M))
    #for a in range (N):
     #   for b in range (M):
    #        coeffs[a][b] = W * a * Z * b
   # print(coeffs)
   # newarr = [[0 for x in range(M)] for y in range(N)]
    #print(l)
    #print(n)
   # print(m)
   # print(M)
   # print(k)
    #print(U) 
    print(T)
    #print(np.matmul(T, U))
   # print(x[0])
    #print(x[:,0].reshape(M, 1))
    for a in range(M): #for each column
        # print(x[:,a])
         x[:,a] = np.dot(T, x[:,a])
       #  print(x)
       # print(x[:,a])
    for i in range(N): #for each row
        x[i] = np.dot(U, x[i])
       # print(x[i])
      #  print(x)
    # print(x)
    
#    for a in range(M):
#        for b in range(N):
#            x[a,b] = np.dot(U, x[:,b])*np.dot(T, x[a])

    return x

def FFT(x):
	x = np.asarray(x, dtype=complex)
	N = x.shape[0]

	if N % 2 > 0:
		raise ValueError("size of x must be a power of 2")
	elif N <= 32: #tweek later
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
    elif N <= 32: #tweek later
        return invdft(x)
    else:
        X_even = invFFT(x[::2])
        X_odd = invFFT(x[1::2])
        factor = np.exp(-2j * np.pi * np.arange(N) / N)
        return np.concatenate([X_even + factor[:int(N/2)] * X_odd, X_even + factor[int(N/2):] * X_odd])

def twoDFFTv2(x):
	x = np.asarray(x, dtype=complex)
	N = x.shape[0] #rows
	M = x.shape[1] #columns
	n = np.arange(N) #array from 0 to N-1
	m = np.arange(M) #.reshape(M, 1) #array from 0 to M-1
	k = m.reshape((M, 1))
	l = n.reshape((1, N))
#	T = np.exp(-2j * np.pi * k * m / M) #inner
#	U = np.exp(-2j * np.pi * l * n / N) #outer

	for i in range(N): #for each row
		#x[i] = np.dot(U, FFT(x[i]))
		x[i] = invFFT(x[i])
	for a in range(M): #for each column
		#x[:,a] = np.dot(T, FFT(x[:,a]))
		x[:,a] = invFFT(x[:,a])
	
	return x

def invtwoDFFT(x):
    x = np.asarray(x, dtype=complex)
    N = x.shape[0] #rows
    M = x.shape[1] #columns
    n = np.arange(N) #array from 0 to N-1
    m = np.arange(M) #.reshape(M, 1) #array from 0 to M-1
    k = m.reshape((M, 1))
    l = n.reshape((1, N))

    for i in range(N): #for each row
		#x[i] = np.dot(U, FFT(x[i]))
        x[i] = invFFT(x[i])
    for a in range(M): #for each column
		#x[:,a] = np.dot(T, FFT(x[:,a]))
        x[:,a] = invFFT(x[:,a])

    return x

def mode_1(image):
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

	#cv2.imshow("Image Resized", im)
	#cv2.imshow("Image Resized", resized)
	
	#--------------#
	#TEST
	x = np.random.random((256,128))
	print("x")
	#print(x)
	print("here1")
	#print(np.fft.fft2(x))
	print("here2")
	#print(twoDFFTv2(x))
	#END TEST
	#--------------#
	
	correctfft = np.fft.fft2(resized)
	twodfft = twoDFFTv2(resized)
	
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

	#cv2.imshow("Image Resized", im)
    cv2.imshow("Image Resized", resized)

    #-----------------#
    #TEST
    x = np.random.random((2,2))
    print("x")
    print(x)
    print(np.fft.ifft2(x))
    print(invtwoDFFT(x))
    #END TEST
    #-----------------#

    twodfft = twoDFFTv2(resized)
    for a in range(twodfft.shape[1]):
        for b in range(twodfft.shape[0]):
            if twodfft[b][a] >= 0.25 * np.pi or twodfft[b][a] <= 0.75 * np.pi: #tweak later
                twodfft[b][a] = 0

    #we removed high frequencies and replaced them by 0
    #output to cmd line number of nonzeroes we used and fraction???

    itwodfft = invtwoDFFT(twodfft)
    correcti2dfft = np.fft.ifft2(twodfft)

    # A logarithmic colormap
    plt.figure()
    plt.imshow(np.abs(correcti2dfft), norm=LogNorm(vmin=5))
    plt.colorbar()
    plt.title('Correct Fourier transform')
    
    plt.figure()
    plt.imshow(np.abs(itwodfft), norm=LogNorm(vmin=5))
    plt.colorbar()
    plt.title('Our Fourier transform')
    plt.show()
    plt.show()
    cv2.waitKey(0)


def mode_3(image):
	print(image)
	
def mode_4(image):
	print(image)
	power = [5,6,7,8,9,10]
	runtimeN = [0,0,0,0,0,0]
	standdevN = [0,0,0,0,0,0] 
	standerrorN = [0,0,0,0,0,0] 
	runtimeF = [0,0,0,0,0,0]
	standdevF = [0,0,0,0,0,0] 
	standerrorF = [0,0,0,0,0,0] 
	i=0
	
	while i < len(power):
		j=0
		#timeN = [0,0,0,0,0,0] 
		#timeF = [0,0,0,0,0,0] 
		timeN = [0, 0, 0] 
		timeF = [0, 0, 0] 
		
		#run it 10 times
		while j < len(timeN):
			x = np.random.random((2**power[i],2**power[i]))
			# naive run
			startN = int(round(time.time() * 1000))
			naive = np.fft.fft2(x) #replace by our own algorithm
			endN = int(round(time.time() * 1000))
			timeN[j] = endN-startN
			
			# fft run
			startF = int(round(time.time() * 1000))
			fft = twoDFFTv2(x) #replace by our own algorithm
			endF = int(round(time.time() * 1000))
			timeF[j] = endF-startF
			
			j=j+1
		#compute run times
		runtimeN[i]= np.mean(timeN)
		standdevN[i] = np.std(timeN)
		runtimeF[i]= np.mean(timeF)
		standdevF[i] = np.std(timeF)
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
	print("Input: ", end='')
	print(n)
	
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
