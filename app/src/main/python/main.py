from skimage.metrics import structural_similarity
import cv2
import numpy as np
from os.path import dirname, join
import os
import io
import base64
from PIL import Image

def main(refdata, sketchdata):
    try:
        ref_decodedata = base64.b64decode(refdata)
        sketch_decodedata = base64.b64decode(sketchdata)
        np_ref = np.fromstring(ref_decodedata, np.uint8)
        np_sketch = np.fromstring(sketch_decodedata, np.uint8)

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
        cv2.drawContours(mask, contours, -1, (255, 255, 255), -1)
        ssim_output_path = join(dirname(__file__), "ssimoutput.png")
        ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=2)

        err = []
        for c in contours:
            area = cv2.contourArea(c)
            if area > 100:
                temp = second_img.copy()
                cv2.drawContours(second_img, [c], 0, (0, 255, 0), -1)
                cv2.drawContours(reference_img, [c], 0, (0, 255, 0), -1)
                cv2.drawContours(temp, [c], 0, (0, 255, 0), -1)
                err.append(temp)

        output_img = err[-1]
        ssim_output = cv2.cvtColor(ssim_output, cv2.COLOR_BGR2RGB)
        cv2.imwrite(ssim_output_path, ssim_output)

        pilimg = Image.fromarray(ssim_output)
        buff = io.BytesIO()
        pilimg.save(buff, format="PNG")
        img_str = base64.b64encode(buff.getvalue()).decode('utf-8')
        final_score = "{:.3f}%".format(score * 100)

        return img_str, final_score

    except Exception as e:
        print("An error occurred:", str(e))
        return None


# def main():
#     try:
#         reference = join(dirname(__file__), "reference.jpg")
#         print(reference)
#         # reference = "./reference.jpg"
#         second = join(dirname(__file__), "input.png")
#         reference_img = cv2.imread(reference)
#         second_img = cv2.imread(second)
#         first_gray = cv2.Canny(reference_img, 100, 200, 3, L2gradient=True)
#         second_gray = 255 - cv2.cvtColor(second_img, cv2.COLOR_BGR2GRAY)
#         output_path = join(dirname(__file__), "first_gray.png")
#         cv2.imwrite(output_path, first_gray)
#         print("Image saved successfully!")
#         score, diff = structural_similarity(first_gray, second_gray, full=True)
#         print("SSIM SCORE: {:.3f}%".format(score * 100))
#         diff = (diff * 255).astype("uint8")
#         _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
#         contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
#         mask = np.zeros(first_gray.shape, dtype='uint8')
#         cv2.drawContours(mask, contours, -1, (255, 255, 255), -1)
#         ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=2)
#         ssim_output_path = join(dirname(__file__), "ssimoutput.png")
#         cv2.imwrite(ssim_output_path, ssim_output)
#         #cv2.imwrite('ssim_output.png', ssim_output)
#         bytearrayOutput = ssim_output.tobytes()
#         # return bytearrayOutput
# #         return bytearrayOutput
#     except Exception as e:
#         print("An error occurred:", str(e))
#         return None
