from skimage.metrics import structural_similarity 
import cv2
import numpy as np
from os.path import dirname, join
import os
import io
import base64
from PIL import Image

# def gpt(refdata,sketchdata):
        
#     try:
#         ref_decodedata = base64.b64decode(refdata)
#         sketch_decodedata = base64.b64decode(sketchdata)
#         np_ref = np.fromstring(ref_decodedata, np.uint8)
#         np_sketch = np.fromstring(sketch_decodedata, np.uint8)

#         reference_img = cv2.imdecode(np_ref, cv2.IMREAD_UNCHANGED)
#         second_img = cv2.imdecode(np_sketch, cv2.IMREAD_UNCHANGED)

#         refpath = join(dirname(__file__), "ref.png")
#         cv2.imwrite(refpath, reference_img)

#         inpath = join(dirname(__file__), "inp.png")
#         cv2.imwrite(inpath, second_img)

#         refopened = cv2.imread(refpath)
#         secondopened = cv2.imread(inpath)

#         secondinvt = cv2.bitwise_not(secondopened)
#         cv2.imwrite(inpath, secondinvt)
#         first_gray = cv2.Canny(refopened, 100, 200, 3, L2gradient=True)
#         output_path = join(dirname(__file__), "cannyoutput.png")
#         cv2.imwrite(output_path, first_gray)
#         print("Canny Image saved successfully!")
        
#         canny = cv2.imread(output_path)
#         canny_gray = cv2.cvtColor(canny,cv2.COLOR_BGR2GRAY)

#         second_gray = 255 - cv2.cvtColor(secondinvt, cv2.COLOR_BGR2GRAY)
#         score, diff = ssim(canny_gray, second_gray, full=True)

#         print("SSIM SCORE: {:.3f}".format(score))

#         diff = (diff * 255).astype("uint8")
#         _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
#         contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

#         mask = np.zeros(first_gray.shape, dtype='uint8')
#         cv2.drawContours(mask, contours, -1, (0, 0, 0), -1)

#         ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=1)
#         ssim_output_path = join(dirname(__file__), "ssimoutput.png")
#         cv2.imwrite(ssim_output_path, ssim_output)

#         pilimg = Image.fromarray(ssim_output)
#         buff = io.BytesIO()
#         pilimg.save(buff, format="PNG")
#         img_str = base64.b64encode(buff.getvalue()).decode('utf-8')

#         final_score = "{:.3f}".format(score)

#         return img_str, final_score

#     except Exception as e:
#         print("An error occurred:", str(e))
#         return None
    

def main2(refdata, sketchdata):
    try:
            
            ref_decodedata = base64.b64decode(refdata)
            sketch_decodedata = base64.b64decode(sketchdata)
            np_ref = np.fromstring(ref_decodedata,np.uint8)
            np_sketch = np.fromstring(sketch_decodedata,np.uint8)

            reference_img = cv2.imdecode(np_ref, cv2.IMREAD_UNCHANGED)
            second_img = cv2.imdecode(np_sketch, cv2.IMREAD_UNCHANGED)
            # reference = "./reference.jpg"
            first_gray = cv2.Canny(reference_img, 100, 200, 3, L2gradient=True)
            second_gray = 255 - cv2.cvtColor(second_img, cv2.COLOR_BGR2GRAY)
            output_path = join(dirname(__file__), "cannyoutput.png")
            cv2.imwrite(output_path, first_gray)
            print("Canny Image saved successfully!")
            score, diff = structural_similarity(first_gray, second_gray, full=True)
           
            # print("SSIM SCORE: {:.3f}%".format(score * 100))
            diff = (diff * 255).astype("uint8")
            _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
            contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            mask = np.zeros(first_gray.shape, dtype='uint8')
            cv2.drawContours(mask, contours, -1, (0, 0, 0), -1)
            ssim_output_path = join(dirname(__file__), "ssimoutput.png")
            ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=2)
            ssim_output = cv2.cvtColor(ssim_output, cv2.COLOR_BGR2RGB)
            cv2.imwrite(ssim_output_path, ssim_output)

            
            # img_encode = cv2.imencode('.png', ssim_output).tobytes()
            # ssim_output_string = base64.b64decode(img_encode)

            pilimg = Image.fromarray(ssim_output)
            buff = io.BytesIO()
            pilimg.save(buff,format="PNG")
            # img_str = base64.b64encode(buff.getvalue())
            img_str = base64.b64encode(buff.getvalue()).decode('utf-8')
            final_score = "{:.3f}%".format(score*100)

            # return bytearrayOutput
            # return ""+str(img_str,'utf-8')
            return img_str, final_score
        
    except Exception as e:
            print("An error occurred:", str(e))
            return None


# def compare(refdata, sketchdata):
#         try: 
#                 ref_decodedata = base64.b64decode(refdata)
#                 sketch_decodedata = base64.b64decode(sketchdata)
#                 np_ref = np.fromstring(ref_decodedata,np.uint8)
#                 np_sketch = np.fromstring(sketch_decodedata,np.uint8)

#                 first = cv2.imdecode(np_ref, cv2.IMREAD_UNCHANGED)
#                 second = cv2.imdecode(np_sketch, cv2.IMREAD_UNCHANGED)

#                 # Convert images to grayscale
#                 first_gray = cv2.cvtColor(first, cv2.COLOR_BGR2GRAY)
#                 second_gray = cv2.cvtColor(second, cv2.COLOR_BGR2GRAY)

#                 # Compute SSIM between two images
#                 score, diff = structural_similarity(first_gray, second_gray, full=True)
#                 print("SSIM SCORE: {:.3f}%".format(score * 100))


#                 # The diff image contains the actual image differences between the two images
#                 # and is represented as a floating point data type so we must convert the array
#                 # to 8-bit unsigned integers in the range [0,255] before we can use it with OpenCV
#                 diff = (diff * 255).astype("uint8")

#                 # Threshold the difference image, followed by finding contours to
#                 # obtain the regions that differ between the two images
#                 thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)[1]
#                 contours = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
#                 contours = contours[0] if len(contours) == 2 else contours[1]

#                 # Highlight differences
#                 mask = np.zeros(first.shape, dtype='uint8')
#                 ssim_output = second.copy()

#                 #For iterable results
#                 duplicate_input = second.copy()

#                 err = []

#                 i=0
#                 for c in contours:
#                         area = cv2.contourArea(c)
#                         if area > 100:
                       
#                                 temp = duplicate_input.copy()

#                                 x, y, w, h = cv2.boundingRect(c)
#                                 cv2.rectangle(first, (x, y), (x + w, y + h), (36, 255, 12), 2)
#                                 cv2.rectangle(second, (x, y), (x + w, y + h), (36, 255, 12), 2)

#                                 cv2.rectangle(temp, (x, y), (x + w, y + h), (36, 255, 12), 2)  #Bounding box for iterable results stored in temp image

#                                 cv2.drawContours(mask, [c], 0, (0, 255, 0), -1)

#                                 cv2.drawContours(ssim_output, [c], 0, (0, 255, 0), -1)   #--All errors saved on final result image ssim_output

#                                 cv2.drawContours(temp, [c], 0, (0, 255, 0), -1)        #Iterable errors stored in temp image

#                                 err.append(temp)


#                 ssim_output_path = join(dirname(__file__), "ssimoutput.png")
#                 cv2.imwrite(ssim_output_path, ssim_output)

            
#             # img_encode = cv2.imencode('.png', ssim_output).tobytes()
#             # ssim_output_string = base64.b64decode(img_encode)

#                 pilimg = Image.fromarray(ssim_output)
#                 buff = io.BytesIO()
#                 pilimg.save(buff,format="PNG")
#                 # img_str = base64.b64encode(buff.getvalue())
#                 img_str = base64.b64encode(buff.getvalue()).decode('utf-8')
#                 final_score = "{:.3f}%".format(score*100)

#                 # return bytearrayOutput
#                 # return ""+str(img_str,'utf-8')
#                 return img_str, final_score

#         except Exception as e :
                     
#                 print("An error occurred:", str(e))
#                 return None

def main():
    try:
            
            refpath = join(dirname(__file__), "umbrella.jpg")
            inputpath = join(dirname(__file__), "umbrellainput3.png")
            reference_img = cv2.imread(refpath)
            second_img = cv2.imread(inputpath)
            # reference = "./reference.jpg"
        #     first_gray = cv2.Canny(reference_img, 100, 200, 3, L2gradient=True)
            first_gray = 255 - cv2.cvtColor(reference_img, cv2.COLOR_BGR2GRAY)
            second_gray = 255 - cv2.cvtColor(second_img, cv2.COLOR_BGR2GRAY)
            output_path = join(dirname(__file__), "cannyoutput.png")
            cv2.imwrite(output_path, first_gray)
            print("Canny Image saved successfully!")
            score, diff = structural_similarity(first_gray, second_gray, full=True)
           
            print("SSIM SCORE: {:.3f}%".format(score * 100))
            diff = (diff * 255).astype("uint8")
            _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
            contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            mask = np.zeros(first_gray.shape, dtype='uint8')
            cv2.drawContours(mask, contours, -1, (0, 0, 0), -1)
            ssim_output_path = join(dirname(__file__), "ssimoutput.png")
            ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=2)
            ssim_output = cv2.cvtColor(ssim_output, cv2.COLOR_BGR2RGB)
            cv2.imwrite(ssim_output_path, ssim_output)

        
            pilimg = Image.fromarray(ssim_output)
            buff = io.BytesIO()
            pilimg.save(buff,format="PNG")
            # img_str = base64.b64encode(buff.getvalue())
            img_str = base64.b64encode(buff.getvalue()).decode('utf-8')
            final_score = "{:.3f}%".format(score*100)

            # return bytearrayOutput
            # return ""+str(img_str,'utf-8')
           
        
    except Exception as e:
            print("An error occurred:", str(e))
            return None


# Orignal Carson
# def main():
#     reference = "reference.jpg"
#     second = "input.png"
#     first_gray = cv2.Canny(reference, 100, 200, 3, L2gradient=True)
#     second_gray = 255 - cv2.cvtColor(second, cv2.COLOR_BGR2GRAY)
#     score, diff = structural_similarity(first_gray, second_gray, full=True)
#     print("SSIM SCORE: {:.3f}%".format(score * 100))
#     diff = (diff * 255).astype("uint8")
#     _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
#     contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
#     mask = np.zeros(first_gray.shape, dtype='uint8')
#     cv2.drawContours(mask, contours, -1, (255, 255, 255), -1)
#     ssim_output = cv2.drawContours(second.copy(), contours, -1, (0, 0, 255), thickness=2)
#     cv2.imwrite('ssim_output.png', ssim_output)