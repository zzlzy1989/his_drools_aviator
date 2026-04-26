#!/usr/bin/env python3
"""
Generate PDF from Markdown for Harness Promotion Plan
Uses fpdf2 with Chinese font support
"""

import markdown
from fpdf import FPDF
import re
import html
import sys
import os

class ChinesePDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=25)
        self.add_font('Noto', '', '/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc', uni=True)
        self.add_font('Noto', 'B', '/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc', uni=True)
        self.add_font('Noto', 'I', '/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc', uni=True)
        
    def header(self):
        if self.page_no() > 1:
            self.set_font('Noto', 'I', 8)
            self.set_text_color(128, 128, 128)
            self.cell(0, 10, 'Harness 工程体系 - 企业内部推广方案', 0, 0, 'L')
            self.cell(0, 10, f'第 {self.page_no()} 页', 0, 1, 'R')
            self.ln(5)
    
    def footer(self):
        self.set_y(-15)
        self.set_font('Noto', 'I', 8)
        self.set_text_color(128, 128, 128)
        self.cell(0, 10, f'第 {self.page_no()}/{{nb}} 页', 0, 0, 'C')
    
    def chapter_title(self, txt):
        self.set_font('Noto', 'B', 16)
        self.set_text_color(0, 51, 102)
        self.ln(5)
        self.cell(0, 12, txt, 0, 1, 'L')
        self.set_draw_color(0, 51, 102)
        self.line(10, self.get_y(), 200, self.get_y())
        self.ln(5)
    
    def section_title(self, txt):
        self.set_font('Noto', 'B', 13)
        self.set_text_color(0, 102, 153)
        self.ln(3)
        self.cell(0, 10, txt, 0, 1, 'L')
        self.ln(2)
    
    def subsection_title(self, txt):
        self.set_font('Noto', 'B', 11)
        self.set_text_color(51, 51, 51)
        self.ln(2)
        self.cell(0, 8, txt, 0, 1, 'L')
        self.ln(1)
    
    def body_text(self, txt):
        self.set_font('Noto', '', 10)
        self.set_text_color(0, 0, 0)
        self.multi_cell(0, 6, txt)
        self.ln(2)
    
    def bullet_point(self, txt, indent=10):
        self.set_font('Noto', '', 10)
        self.set_text_color(0, 0, 0)
        x = self.get_x()
        self.set_x(x + indent)
        self.cell(5, 6, '•', 0, 0)
        self.multi_cell(0, 6, txt)
        self.ln(1)

def clean_html(text):
    """Clean HTML tags and convert basic markdown"""
    # Remove HTML tags
    text = re.sub(r'<[^>]+>', '', text)
    # Convert HTML entities
    text = html.unescape(text)
    return text

def parse_markdown(md_content):
    """Parse markdown content into structured elements"""
    lines = md_content.split('\n')
    elements = []
    i = 0
    
    while i < len(lines):
        line = lines[i]
        
        # Skip empty lines
        if not line.strip():
            i += 1
            continue
        
        # Chapter titles (# )
        if line.startswith('# ') and not line.startswith('## '):
            elements.append(('chapter', clean_html(line[2:].strip())))
            i += 1
            continue
        
        # Section titles (## )
        if line.startswith('## ') and not line.startswith('### '):
            elements.append(('section', clean_html(line[3:].strip())))
            i += 1
            continue
        
        # Subsection titles (### )
        if line.startswith('### '):
            elements.append(('subsection', clean_html(line[4:].strip())))
            i += 1
            continue
        
        # Bullet points
        if line.strip().startswith('- ') or line.strip().startswith('* '):
            bullet_text = clean_html(line.strip()[2:])
            elements.append(('bullet', bullet_text))
            i += 1
            continue
        
        # Table rows (skip for now, will handle separately)
        if line.strip().startswith('|'):
            # Collect table
            table_lines = []
            while i < len(lines) and lines[i].strip().startswith('|'):
                table_lines.append(lines[i].strip())
                i += 1
            
            # Parse table
            if len(table_lines) >= 2:
                headers = [h.strip() for h in table_lines[0].split('|')[1:-1]]
                rows = []
                for row_line in table_lines[2:]:  # Skip header and separator
                    cells = [c.strip() for c in row_line.split('|')[1:-1]]
                    rows.append(cells)
                elements.append(('table', (headers, rows)))
            continue
        
        # Regular text (including bold)
        if line.strip():
            text = clean_html(line.strip())
            # Handle bold text
            text = re.sub(r'\*\*(.+?)\*\*', r'\1', text)
            elements.append(('text', text))
            i += 1
            continue
        
        i += 1
    
    return elements

def generate_pdf(md_file, pdf_file):
    """Generate PDF from markdown file"""
    with open(md_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Skip the front matter (title, version, etc.)
    content = re.sub(r'^---.*?---\n', '', content, flags=re.DOTALL)
    
    elements = parse_markdown(content)
    
    pdf = ChinesePDF()
    pdf.alias_nb_pages()
    pdf.add_page()
    
    # Title page
    pdf.ln(40)
    pdf.set_font('Noto', 'B', 28)
    pdf.set_text_color(0, 51, 102)
    pdf.cell(0, 15, 'Harness 工程体系', 0, 1, 'C')
    pdf.ln(5)
    pdf.set_font('Noto', 'B', 20)
    pdf.set_text_color(0, 102, 153)
    pdf.cell(0, 12, '企业内部推广方案', 0, 1, 'C')
    pdf.ln(15)
    pdf.set_font('Noto', '', 12)
    pdf.set_text_color(100, 100, 100)
    pdf.cell(0, 8, 'AI 辅助开发的标准化工程框架', 0, 1, 'C')
    pdf.cell(0, 8, '从"经验驱动"到"系统约束"的研发效能升级', 0, 1, 'C')
    pdf.ln(20)
    pdf.set_font('Noto', '', 10)
    pdf.cell(0, 6, '版本: v1.0', 0, 1, 'C')
    pdf.cell(0, 6, '编制日期: 2026-04-26', 0, 1, 'C')
    pdf.cell(0, 6, '适用对象: 技术管理层、研发总监、CTO', 0, 1, 'C')
    
    # Process elements
    for elem_type, elem_content in elements:
        if elem_type == 'chapter':
            pdf.add_page()
            pdf.chapter_title(elem_content)
        elif elem_type == 'section':
            pdf.section_title(elem_content)
        elif elem_type == 'subsection':
            pdf.subsection_title(elem_content)
        elif elem_type == 'bullet':
            pdf.bullet_point(elem_content)
        elif elem_type == 'table':
            headers, rows = elem_content
            # Simple table rendering
            col_width = 180 / max(len(headers), 1)
            pdf.set_font('Noto', 'B', 9)
            pdf.set_fill_color(0, 51, 102)
            pdf.set_text_color(255, 255, 255)
            for h in headers:
                pdf.cell(col_width, 8, h[:15], 1, 0, 'C', True)
            pdf.ln()
            pdf.set_font('Noto', '', 8)
            pdf.set_text_color(0, 0, 0)
            pdf.set_fill_color(240, 240, 240)
            fill = False
            for row in rows[:15]:  # Limit rows to prevent overflow
                for j, cell in enumerate(row):
                    pdf.cell(col_width, 7, cell[:15], 1, 0, 'L', fill)
                pdf.ln()
                fill = not fill
            pdf.ln(3)
        elif elem_type == 'text':
            pdf.body_text(elem_content)
    
    pdf.output(pdf_file)
    print(f"PDF generated: {pdf_file}")

if __name__ == '__main__':
    md_file = '/home/gaoxu/Documents/trae_projects/gx_project/his_drools_aviator/.trae/HARNESS_PROMOTION_PLAN.md'
    pdf_file = '/home/gaoxu/Documents/trae_projects/gx_project/his_drools_aviator/.trae/HARNESS_PROMOTION_PLAN.pdf'
    
    if not os.path.exists(md_file):
        print(f"Error: {md_file} not found")
        sys.exit(1)
    
    generate_pdf(md_file, pdf_file)
