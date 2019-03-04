from trec_car.read_data import *
from sklearn.feature_extraction.text import TfidfVectorizer
import json

i = 0
id_set = []


def recursive_paras(p):
    global i
    global id_set
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            recursive_paras(ch)


def find_num_paras(file):
    global i
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            recursive_paras(s)
    print(i)


def search_article(p, id):
    global i
    if isinstance(p, Para):
        fp = r'article_test200_corpus_v3\paraDoc' + str(i) + '.trectext'
        wf = open(fp, 'wb')
        wf.write(b"<DOC>\n")
        wf.write(b"<DOCNO>")
        wf.write((p.paragraph.para_id).encode('utf8'))
        wf.write(b"</DOCNO>\n")
        wf.write(b"<HEADLINE>")
        wf.write(id.encode('utf8'))
        wf.write(b"</HEADLINE>\n")
        wf.write(b"<TEXT>\n")
        wf.write((p.get_text()).encode('utf8'))
        wf.write(b"\n</TEXT>\n")
        wf.write(b"</DOC>\n")
        i += 1
        return 1
    elif isinstance(p, Section):
        for ch in p.children:
            search_article(ch, id)
        return 1


def search_paras(p, id):
    global i
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            fp = r'hierarchical_test200_corpus_v2\paraDoc' + str(i) + '.trectext'
            wf = open(fp, 'wb')
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.paragraph.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<HEADLINE>")
            wf.write(id.encode('utf8'))
            wf.write(b"</HEADLINE>\n")
            wf.write(b"<TEXT>\n")
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n")
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            search_paras(ch, id+'/'+p.heading)


def search_toplevel(p, id, level):
    global i
    if isinstance(p, Para):
        fp = r'toplevel_test200_corpus_v2\paraDoc' + str(i) + '.trectext'
        wf = open(fp, 'wb')
        wf.write(b"<DOC>\n")
        wf.write(b"<DOCNO>")
        wf.write((p.paragraph.para_id).encode('utf8'))
        wf.write(b"</DOCNO>\n")
        wf.write(b"<HEADLINE>")
        wf.write(id.encode('utf8'))
        wf.write(b"</HEADLINE>\n")
        wf.write(b"<TEXT>\n")
        wf.write((p.get_text()).encode('utf8'))
        wf.write(b"\n</TEXT>\n")
        wf.write(b"</DOC>\n")
        i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            if level == 0:
                search_toplevel(ch, id+'/'+p.heading, level+1)
            else:
                search_toplevel(ch, id, level+1)


def parse_annotations(file):
    j = 0
    for p in iter_annotations(open(file, 'rb')):
        #temp = str(p.page_name) + '\n' + str(p.page_meta) + '\n'.join(str(s) for s in p.skeleton)
        #fp = r'corpus\test'+str(i)+'.txt'
        #wr_file = open(fp,'w')
        #wr_file.write(str(temp.encode('UTF-8')))
        j += 1
        headings = p.nested_headings()
        print(p.page_id)
        print('headings= ', [(str(section.heading), len(children)) for (section, children) in headings])
        print([len(t) for t in p.flat_headings_list()])
        if len(p.outline()) > 0:
            print('deep headings= ',
                  [(str(section.heading), len(children)) for (section, children) in p.deep_headings_list()])
            print('flat headings= ',
                  ["/".join([str(section.heading) for section in sectionpath]) for sectionpath in p.flat_headings_list()])
        print("\n----------------------------------------------------\n")
    print(j)


def parse_paragraphs(file):
    j = 0
    for p in iter_paragraphs(open(file, 'rb')):
        print('\n', p.para_id, ':')
        # Print just the text
        print('Simple text\n\n')
        texts = [elem.text if isinstance(elem, ParaText) else elem.anchor_text for elem in p.bodies]
        print(' '.join(texts))

        # Print just the linked entities
        print('Entities\n\n')
        entities = [elem.page for elem in p.bodies if isinstance(elem, ParaLink)]
        print(entities)

        # Print text interspersed with links as pairs (text, link)
        print('Mixed\n\n')
        mixed = [(elem.anchor_text, elem.page) if isinstance(elem, ParaLink) else (elem.text, None) for elem in p.bodies]
        print(mixed)

        print('all of it\n\n')
        print(p.get_text())
        print("\n--------------------------------------------------------------------------------------------------------\n")
        j += 1
    print(j)


def make_hierarchical_corpus(file):
    global i
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_paras(s, p.page_name)
    print(i)


def make_article_corpus(file):
    i = 0
    for p in iter_annotations(open(file, 'rb')):
        fp = r'article_test200_corpus\articleDoc' + str(i) + '.trectext'
        wf = open(fp, 'wb')
        wf.write(b"<DOC>\n")
        wf.write(b"<DOCNO>")
        wf.write((p.page_id).encode('utf8'))
        wf.write(b"</DOCNO>\n")
        wf.write(b"<TITLE>")
        wf.write((p.page_name).encode('utf8'))
        wf.write(b"</TITLE>\n")
        wf.write(b"<TEXT>\n")
        article_text = (''.join(str(s) for s in p.skeleton)).encode('utf8')
        wf.write(article_text)
        wf.write(b"\n</TEXT>\n")
        wf.write(b"</DOC>\n")
        i+=1
    print(i)


def make_article_corpus_v2(file):
    global i
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_article(s, p.page_name)
    print(i)


def make_toplevel_corpus(file):
    global i
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_toplevel(s, p.page_name, 0)
    print(i)


def create_queries(file):
    fp = r'test200_queries.json'
    wf = open(fp, 'w')
    data = {}
    data['index'] = r'C:\Users\Vasilis\Documents\galagoTrials\test200_trial1'
    #data['queryType'] = 'complex'
    data['requested'] = 10
    data['processingModel'] = 'org.lemurproject.galago.core.retrieval.processing.RankedDocumentModel'
    data['scorer'] = 'bm25'
    data['queries'] = []
    j = 1
    for p in iter_annotations(open(file, 'rb')):
        data['queries'].append({'number':str(j), 'text':'#combine('+p.page_name+')'})
        j+=1
        #wf.write((p.page_name).encode('utf8'))
        #wf.write(b"\n")
        #print([" ".join([str(section.heading) for section in sectionpath]) for sectionpath in p.flat_headings_list()])
        for q in [" ".join([str(section.heading) for section in sectionpath]) for sectionpath in p.flat_headings_list()]:
            data['queries'].append({'number': str(j), 'text': '#combine('+p.page_name+' '+q+')'})
            # wf.write((p.page_name).encode('utf8'))
            # wf.write(b" ")
            # wf.write(q.encode('utf8'))
            # wf.write(b"\n")
            j+=1
    json.dump(data, wf)

file = r'test200-train\train.pages.cbor'
#file = r'benchmarkY1\benchmarkY1-train\fold-0-train.pages.cbor-outlines.cbor'
#parse_annotations(file)
#make_article_corpus(file)
#make_article_corpus_v2(file)
make_hierarchical_corpus(file)
#make_toplevel_corpus(file)
#create_queries(file)
file = r'test200-train\train.pages.cbor-paragraphs.cbor'
#file = r'benchmarkY1\benchmarkY1-train\fold-0-train.pages.cbor-paragraphs.cbor'
#parse_paragraphs(file)
#file = r'benchmarkY1\benchmarkY1-train\fold-0-train.pages.cbor'
file = r'test200-train\train.pages.cbor'
#find_num_paras(file)
#create_queries(file)

