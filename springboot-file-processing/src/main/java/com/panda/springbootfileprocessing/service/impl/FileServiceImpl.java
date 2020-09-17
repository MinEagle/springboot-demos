package com.panda.springbootfileprocessing.service.impl;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import com.panda.springbootfileprocessing.service.FileService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author YIN
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public MultipartFile addWatermark(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension = StringUtils.stripFilenameExtension(filename);
        String fileType = StringUtils.getFilenameExtension(filename);
        String contentType = file.getContentType();
        assert contentType != null;
        String substring = contentType.substring(0, contentType.indexOf("/"));
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        fileType = "image".equals(substring) ? substring : fileType;
        System.out.println(contentType);
        switch (Objects.requireNonNull(fileType)) {
            case "image":
                arrayOutputStream = this.addTextWatermarkPicture(file);
                break;
            case "pdf":
                arrayOutputStream = this.addTextWatermarkPDDocument(file);
                break;
            case "docx":
            case "xlsx":
                arrayOutputStream = this.addTextWatermarkOfficeDocuments(file);
                break;
            default:
                break;
        }
        try {
            file = new MockMultipartFile(file.getName(), filename, contentType, new ByteArrayInputStream(arrayOutputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private String customText = "哈哈哈啊哈"; // 水印文字
    private String fontName = "微软雅黑"; // word字体
    private String fontSize = "0.5pt"; // 字体大小
    private String fontColor = "#d0d0d0"; // 字体颜色
    private int widthPerWord = 10; // 一个字平均长度，单位pt，用于：计算文本占用的长度（文本总个数*单字长度）
    private String styleTop = "0"; // 与顶部的间距
    private String styleRotation = "45"; // 文本旋转角度

    public FileServiceImpl() {
        customText = customText + repeatString(" ", 8); // 水印文字之间使用8个空格分隔
        this.customText = repeatString(customText, 10); // 一行水印重复水印文字次数
    }

    @Override
    public ByteArrayOutputStream addTextWatermarkOfficeDocuments(MultipartFile file) {
        String fileType = StringUtils.getFilenameExtension(file.getOriginalFilename());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = file.getInputStream()) {
            switch (Objects.requireNonNull(fileType)) {
                case "docx":
                    outputStream = addTextWatermarkOfficeDocumentsWord(inputStream, outputStream);
                    break;
                case "xlsx":
                    outputStream = addTextWatermarkOfficeDocumentsExcle(inputStream, outputStream);
                    break;
                default:
                    int ch;
                    while ((ch = inputStream.read()) != -1) {
                        outputStream.write(ch);
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    public ByteArrayOutputStream addTextWatermarkOfficeDocumentsExcle(InputStream inputStream, ByteArrayOutputStream outputStream) {
        //读取excel文件
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            //获取excel sheet个数
            int sheets = wb.getNumberOfSheets();
            //循环sheet给每个sheet添加水印
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = wb.getSheetAt(i);
                //excel加密只读
//                sheet.protectSheet(UUID.randomUUID().toString());
                //获取excel实际所占行
                int row = sheet.getFirstRowNum() + sheet.getLastRowNum();
                //获取excel实际所占列
                int cell = sheet.getRow(sheet.getFirstRowNum()).getLastCellNum() + 1;
                //根据行与列计算实际所需多少水印
                putWaterRemarkToExcel(wb, sheet, 0, 0, 5, 5, cell / 5 + 1, row / 5 + 1, 0, 0);
            }
            wb.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    public void putWaterRemarkToExcel(Workbook wb, Sheet sheet, int startXCol,
                                      int startYRow, int betweenXCol, int betweenYRow, int XCount, int YCount, int waterRemarkWidth,
                                      int waterRemarkHeight) throws IOException {
        // 加载图片
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();

        Font font = new Font("微软雅黑", Font.PLAIN, 50); //字体
        BufferedImage bi1 = waterMarkByText(300, 200, "测试字体", Color.lightGray, font, -30d, 0.3f);
        ImageIO.write(bi1, "png", byteArrayOut);

        // 开始打水印
        Drawing drawing = sheet.createDrawingPatriarch();

        // 按照共需打印多少行水印进行循环
        for (int yCount = 0; yCount < YCount; yCount++) {
            // 按照每行需要打印多少个水印进行循环
            for (int xCount = 0; xCount < XCount; xCount++) {
                // 创建水印图片位置
                int xIndexInteger = startXCol + (xCount * waterRemarkWidth) + (xCount * betweenXCol);
                int yIndexInteger = startYRow + (yCount * waterRemarkHeight) + (yCount * betweenYRow);
                /*
                 * 参数定义： 第一个参数是（x轴的开始节点）； 第二个参数是（是y轴的开始节点）； 第三个参数是（是x轴的结束节点）；
                 * 第四个参数是（是y轴的结束节点）； 第五个参数是（是从Excel的第几列开始插入图片，从0开始计数）；
                 * 第六个参数是（是从excel的第几行开始插入图片，从0开始计数）； 第七个参数是（图片宽度，共多少列）；
                 * 第8个参数是（图片高度，共多少行）；
                 */
                ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, xIndexInteger,
                        yIndexInteger, xIndexInteger + waterRemarkWidth, yIndexInteger + waterRemarkHeight);

                Picture pic = drawing.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_PNG));
                pic.resize();
            }
        }
    }

    private ByteArrayOutputStream addTextWatermarkOfficeDocumentsWord(InputStream inputStream, OutputStream outputStream) {
        try (BufferedInputStream buffIn = new BufferedInputStream(inputStream);
             XWPFDocument doc = new XWPFDocument(buffIn)) {
            // 遍历文档，添加水印
            for (int lineIndex = -10; lineIndex < 10; lineIndex++) {
                styleTop = 200 * lineIndex + " ";
                waterMarkDocXDocument(doc);
            }
            doc.write(outputStream); // 写出添加水印后的文档
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ByteArrayOutputStream) outputStream;
    }

    /**
     * 为文档添加水印<br />
     * 实现参考了{@link org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy#(String, int)}
     *
     * @param doc 需要被处理的docx文档对象
     */
    private void waterMarkDocXDocument(XWPFDocument doc) {
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT); // 如果之前已经创建过 DEFAULT 的Header，将会复用之
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        CTP ctp = header.getParagraphArray(0).getCTP();
        byte[] rsidr = doc.getDocument().getBody().getPArray(0).getRsidR();
        byte[] rsidrdefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
        ctp.setRsidP(rsidr);
        ctp.setRsidRDefault(rsidrdefault);
        CTPPr ppr = ctp.addNewPPr();
        ppr.addNewPStyle().setVal("Header");
        // 开始加水印
        CTR ctr = ctp.addNewR();
        CTRPr ctrpr = ctr.addNewRPr();
        ((CTRPr) ctrpr).addNewNoProof();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapetype = group.addNewShapetype();
        CTTextPath shapeTypeTextPath = shapetype.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTLock lock = shapetype.addNewLock();
        lock.setExt(STExt.VIEW);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject");
        shape.setSpid("_x0000_s102");
        shape.setType("#_x0000_t136");
        shape.setStyle(getShapeStyle()); // 设置形状样式（旋转，位置，相对路径等参数）
        shape.setFillcolor(fontColor);
        shape.setStroked(STTrueFalse.FALSE); // 字体设置为实心
        CTTextPath shapeTextPath = shape.addNewTextpath(); // 绘制文本的路径
        shapeTextPath.setStyle("font-family:" + fontName + ";font-size:" + fontSize); // 设置文本字体与大小
        shapeTextPath.setString(customText);
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    // 构建Shape的样式参数
    private String getShapeStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append("position: ").append("absolute"); // 文本path绘制的定位方式
        sb.append(";width: ").append(customText.length() * widthPerWord).append("pt"); // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";height: ").append("20pt"); // 字体高度
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        sb.append(";top: ").append(styleTop);
        sb.append(";mso-position-horizontal-relative: ").append("page");
        sb.append(";mso-position-vertical-relative: ").append("page");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(styleRotation);
        return sb.toString();
    }

    /**
     * 将指定的字符串重复repeats次.
     */
    private String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }

    @Override
    public ByteArrayOutputStream addTextWatermarkPDDocument(MultipartFile file) {
        //打开pdf文件
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(PDDocument doc = PDDocument.load(file.getInputStream())) {
            doc.setAllSecurityToBeRemoved(true);
            //遍历pdf所有页
            for (PDPage page : doc.getPages()) {
                PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
                String ts = "f0925884";
                //引入字体文件 解决中文汉字乱码问题

                float fontSize = 30;
                PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
                // 水印透明度
                r0.setNonStrokingAlphaConstant(0.2f);
                r0.setAlphaSourceFlag(true);
                cs.setGraphicsStateParameters(r0);
                //水印颜色
                cs.setNonStrokingColor(new Color(158, 158, 158));
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                //根据水印文字大小长度计算横向坐标需要渲染几次水印
                float h = ts.length() * fontSize;
                for (int i = 0; i <= 10; i++) {
                    // 获取旋转实例
                    cs.setTextMatrix(Matrix.getRotateInstance(-150, i * 100, 0));
                    cs.showText(ts);
                    for (int j = 0; j < 20; j++) {
                        cs.setTextMatrix(Matrix.getRotateInstance(-150, i * 100, j * h));
                        cs.showText(ts);
                    }
                }
                cs.endText();
                cs.restoreGraphicsState();
                cs.close();
                doc.save(baos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }

    @Override
    public ByteArrayOutputStream addTextWatermarkPicture(MultipartFile file) {

        // 不透明度
        float opacity = 0.5f;
        int x = 0;
        int y = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // ImageIO读取图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            int width = image.getWidth();
            int heigth = image.getHeight();
            Font font = new Font("微软雅黑", Font.PLAIN, 100); //字体
            BufferedImage bi1 = waterMarkByText(500, 500, "测试字体", Color.lightGray, font, -30d, 1.0f);

            Thumbnails.Builder<BufferedImage> size =
                    Thumbnails.of(image)
                            // 设置图片大小
                            .size(image.getWidth(), image.getHeight())
                            .outputFormat(StringUtils.getFilenameExtension(file.getOriginalFilename()));
            while (x < width) {
                while (y < heigth) {
                    int finalX = x;
                    int finalY = y;
                    size.watermark((int enclosingWidth,
                                    int enclosingHeight,
                                    int widths,
                                    int height,
                                    int insetLeft,
                                    int insetRight,
                                    int insetTop,
                                    int insetBottom) -> new Point(finalX, finalY), bi1, opacity);
                    y += 500;
                }
                y = 0;
                x += 500;
            }
            size.toOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }


    public BufferedImage waterMarkByText(int width, int heigth, String text, Color color,
                                         Font font, Double degree, float alpha) {

        BufferedImage buffImg = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
        /**2、得到画笔对象*/
        Graphics2D g2d = buffImg.createGraphics();
        // ----------  增加下面的代码使得背景透明  -----------------
        buffImg = g2d.getDeviceConfiguration()
                .createCompatibleImage(width, heigth, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = buffImg.createGraphics();
        // ----------  背景透明代码结束  -----------------

        // 设置对线段的锯齿状边缘处理
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 设置水印旋转
        if (null != degree) {
            //注意rotate函数参数theta，为弧度制，故需用Math.toRadians转换一下
            //以矩形区域中央为圆心旋转
            g2d.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2,
                    (double) buffImg.getHeight() / 2);
        }

        // 设置颜色
        g2d.setColor(color);

        // 设置 Font
        g2d.setFont(font);

        //设置透明度:1.0f为透明度 ，值从0-1.0，依次变得不透明
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        //获取真实宽度
        float realWidth = this.getRealFontWidth(text);
        float fontSize = font.getSize();
        //计算绘图偏移x、y，使得使得水印文字在图片中居中
        //这里需要理解x、y坐标是基于Graphics2D.rotate过后的坐标系
        float x = 0.5f * width - 0.5f * fontSize * realWidth;
        float y = 0.5f * heigth + 0.5f * fontSize;

        //取绘制的字串宽度、高度中间点进行偏移，使得文字在图片坐标中居中
        g2d.drawString(text, x, y);
        //释放资源
        g2d.dispose();
        return buffImg;
    }


    /**
     * 获取真实字符串宽度，ascii字符占用0.5，中文字符占用1.0
     */
    private float getRealFontWidth(String text) {
        int len = text.length();
        float width = 0f;
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) < 256) {
                width += 0.5f;
            } else {
                width += 1.0f;
            }
        }
        return width;
    }
}
