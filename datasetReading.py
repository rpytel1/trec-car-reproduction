from trec_car.read_data import *
import json
import stop_words as stpw


i = 0
id_set = {}


def parse_annotations(file):
    """
        A simple function to parse annotations from a cbor trec-car file
        similar to the one found on trec-car-tools github repository
    """
    j = 0
    for p in iter_annotations(open(file, 'rb')):
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
    """
        A simple function to parse paragraphs from a cbor trec-car file
        similar to the one found on trec-car-tools github repository
    """
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
        print("\n---------------------------------------------------------------------------------------------------\n")
        j += 1

    print(j)


def make_article_corpus(file):
    """
        Function to create seperate trectext documents for each article
        in the collection
    """
    i = 0
    for p in iter_annotations(open(file, 'rb')):
        fp = r'article_test200_corpus/articleDoc' + str(i) + '.trectext'
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


def make_big_corpus(file):
    """
        Function to create the bid trectext corpus from the half-wiki
        collection
    """
    global i
    j=0
    fp = r'train_big_corpus'+str(j)+'.trectext'
    wf = open(fp, 'wb')
    for p in iter_paragraphs(open(file, 'rb')):
        if p.para_id not in id_set.keys():
            id_set[p.para_id] = 1
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<TEXT>\n")
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n\n")
            i += 1
        if i >  70000:
            wf.close()
            i = 0
            j+=1
            fp = r'train_big_corpus'+str(j)+'.trectext'
            wf = open(fp, 'wb')
        print(i)
    wf.close()


def search_paras_no_headline(p, wf):
    """
        Recursive function used to extract all paragraphs from
        the small test200 corpus from the outlines cbor file
    """
    global i
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.paragraph.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<TEXT>\n")
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n\n")
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            search_paras_no_headline(ch, wf)


def make_paras(file):
    """
        Function used to create the small test200 corpus through
        calling the recursive search_paras_no_headline function
    """
    global i
    fp = r'paragraph_corpus_test200.trectext'
    wf = open(fp, 'wb')
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_paras_no_headline(s, wf)
    print(i)
    wf.close()


def search_article(p, id, wf):
    """
        Old implementation of a recursive function used to search
        for articles in the cbor files
    """
    global i
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.paragraph.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<HEADLINE>")
            wf.write(id.encode('utf8'))
            wf.write(b"</HEADLINE>\n")
            wf.write(b"<TEXT>\n")
            wf.write(id.encode('utf8'))
            wf.write(b'\n')
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n\n")
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            search_article(ch, id, wf)


def make_article_corpus_v2(file):
    """
        Old implementation of function to create article level corpus
        with the usage also of a headline field
    """
    global i
    fp = r'article_corpus_test200.trectext'
    wf = open(fp, 'wb')
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_article(s, p.page_name, wf)
    print(i)
    wf.close()


def search_paras(p, id, wf):
    """
        Old implementation of a recursive function used to search
        for paragraphs in the cbor files
    """
    global i
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.paragraph.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<HEADLINE>")
            wf.write(id.encode('utf8'))
            wf.write(b"</HEADLINE>\n")
            wf.write(b"<TEXT>\n")
            wf.write((id.replace('/',' ')).encode('utf8'))
            wf.write(b'\n')
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n\n")
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            search_paras(ch, id+'/'+p.heading, wf)


def make_hierarchical_corpus(file):
    """
        Old implementation of function to create hierarchical level corpus
        with the usage also of a headline field
    """
    global i
    fp = r'hierarchical_corpus_test200.trectext'
    wf = open(fp, 'wb')
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_paras(s, p.page_name, wf)
    print(i)
    wf.close()


def search_toplevel(p, id, level, wf):
    """
        Old implementation of a recursive function used to search
        for toplevel sections in the cbor files
    """
    global i
    if isinstance(p, Para):
        if p.paragraph.para_id not in id_set:
            id_set.append(p.paragraph.para_id)
            wf.write(b"<DOC>\n")
            wf.write(b"<DOCNO>")
            wf.write((p.paragraph.para_id).encode('utf8'))
            wf.write(b"</DOCNO>\n")
            wf.write(b"<HEADLINE>")
            wf.write(id.encode('utf8'))
            wf.write(b"</HEADLINE>\n")
            wf.write(b"<TEXT>\n")
            wf.write((id.replace('/', ' ')).encode('utf8'))
            wf.write(b'\n')
            wf.write((p.get_text()).encode('utf8'))
            wf.write(b"\n</TEXT>\n")
            wf.write(b"</DOC>\n\n")
            i += 1
    elif isinstance(p, Section):
        for ch in p.children:
            if level == 0:
                search_toplevel(ch, id+'/'+p.heading, level+1, wf)
            else:
                search_toplevel(ch, id, level+1, wf)


def make_toplevel_corpus(file):
    """
        Old implementation of function to create toplevel level corpus
        with the usage also of a headline field
    """
    global i
    fp = r'toplevel_corpus_test200.trectext'
    wf = open(fp, 'wb')
    for p in iter_annotations(open(file, 'rb')):
        for s in p.skeleton:
            search_toplevel(s, p.page_name, 0, wf)
    print(i)
    wf.close()


def remove_stop_word(s):
    """
        Simple function to remove stopwords from an English based stopword list
    """
    return ' '.join([p for p in s.split() if p not in list(stpw.get_stop_words('en'))])


def create_queries(file):
    """
        Function used to create the json files for all the needed queries
        in the hierarchical level - if the commented for lines are uncommented
        and the above for lines are commented then we can create toplevel queries
        while if the whole inner for loop is commented we can create article level
        queries
    """
    fp = r'train_queries_hierarchical.json'
    wf = open(fp, 'w')
    data = dict()
    data['index'] = r'C:\Users\Vasilis\Documents\galagoTrials\train_paragraph'
    data['requested'] = 10
    data['processingModel'] = 'org.lemurproject.galago.core.retrieval.processing.RankedDocumentModel'
    data['scorer'] = 'bm25'
    data['queries'] = []
    j = 1
    for p in iter_annotations(open(file, 'rb')):
        data['queries'].append({'number': str(p.page_id), 'text': '#combine(' +
                                                                  remove_stop_word(p.page_name) + ')'})
        print(j)
        j += 1
        for (q, q_id) in [(" ".join([str(section.heading)
                                for section in sectionpath]), "/".join([str(section.headingId)
                                for section in sectionpath]))
                                    for sectionpath in p.flat_headings_list()]:
        # for (q, q_id) in [(str(section.heading), str(section.headingId))
        #                       for (section, children) in p.deep_headings_list()]:
        #     #data['queries'].append({'number': str(j), 'text': '#combine('+p.page_name+' '+q+')'})
            data['queries'].append({'number': str(p.page_id+'/'+q_id), 'text': '#combine('
                                                    + remove_stop_word(p.page_name) + ' ' + remove_stop_word(q) + ')'})
            j += 1
            print(j)
    json.dump(data, wf)


def create_queries_for_expansion(file):
    """
            Function used to create the json files for all the needed queries
            with query expansion included (if rm3 is the relevance model then the weight should be specified)
            in the hierarchical level
    """
    fp = r'train_paragraph_exp3_hierarchical.json'
    wf = open(fp, 'w')
    data = dict()
    data['index'] = r'C:\Users\Vasilis\Documents\galagoTrials\train_paragraph'
    data['requested'] = 10
    data['processingModel'] = 'org.lemurproject.galago.core.retrieval.processing.RankedDocumentModel'
    data['relevanceModel'] = 'org.lemurproject.galago.core.retrieval.prf.RelevanceModel3'
    data['scorer'] = 'bm25'
    data['queries'] = []
    j = 1
    for p in iter_annotations(open(file, 'rb')):
        data['queries'].append({'number': str(p.page_id), 'text': '#rm:bOrigWt=0.8:fbDocs=10:fbTerm=10(' +
                                                                  remove_stop_word(p.page_name) + ')'})
        j += 1
        print(j)
        for (q, q_id) in [(" ".join([str(section.heading)
                                for section in sectionpath]), "/".join([str(section.headingId)
                                for section in sectionpath]))
                                    for sectionpath in p.flat_headings_list()]:
            data['queries'].append({'number': str(p.page_id+'/'+q_id), 'text': '#rm:bOrigWt=0.8:fbDocs=10:fbTerm=10('
                                                    + remove_stop_word(p.page_name) + ' ' + remove_stop_word(q) + ')'})
            j += 1
            print(j)
    json.dump(data, wf)


def create_queries_for_expansion_article(file):
    """
        Function used to create the json files for all the needed queries
        with query expansion included (if rm3 is the relevance model then the weight should be specified)
        in the article level
    """
    fp = r'train_paragraph_exp3_article.json'
    wf = open(fp, 'w')
    data = dict()
    data['index'] = r'C:\Users\Vasilis\Documents\galagoTrials\train_paragraph'
    data['requested'] = 20
    data['processingModel'] = 'org.lemurproject.galago.core.retrieval.processing.RankedDocumentModel'
    data['relevanceModel'] = 'org.lemurproject.galago.core.retrieval.prf.RelevanceModel3'
    data['scorer'] = 'bm25'
    data['queries'] = []
    j = 1
    for p in iter_annotations(open(file, 'rb')):
        data['queries'].append({'number': str(p.page_id), 'text': '#rm:bOrigWt=0.8:fbDocs=10:fbTerm=10(' +
                                                                  remove_stop_word(p.page_name) + ')'})
        j += 1
        print(j)
    json.dump(data, wf)


def create_queries_for_expansion_toplevel(file):
    """
            Function used to create the json files for all the needed queries
            with query expansion included (if rm3 is the relevance model then the weight should be specified)
            in the toplevel level
    """
    fp = r'train_paragraph_exp3_toplevel.json'
    wf = open(fp, 'w')
    data = dict()
    data['index'] = r'C:\Users\Vasilis\Documents\galagoTrials\train_paragraph'
    data['requested'] = 20
    data['processingModel'] = 'org.lemurproject.galago.core.retrieval.processing.RankedDocumentModel'
    data['relevanceModel'] = 'org.lemurproject.galago.core.retrieval.prf.RelevanceModel3'
    data['scorer'] = 'bm25'
    data['queries'] = []
    j = 1
    for p in iter_annotations(open(file, 'rb')):
        data['queries'].append({'number': str(p.page_id), 'text': '#rm:bOrigWt=0.8:fbDocs=10:fbTerm=10(' +
                                                                  remove_stop_word(p.page_name) + ')'})
        j += 1
        print(j)
        for (q, q_id) in [(str(section.heading), str(section.headingId))
                              for (section, children) in p.deep_headings_list()]:
            data['queries'].append({'number': str(p.page_id+'/'+q_id), 'text': '#rm:bOrigWt=0.8:fbDocs=10:fbTerm=10('
                                                    + remove_stop_word(p.page_name) + ' ' + remove_stop_word(q) + ')'})
            j += 1
            print(j)
    json.dump(data, wf)


# Examples of the function calls to process cbor files and create corpus or to create query json files

file = r'test200-train\train.pages.cbor'
file = r'train\base.train.cbor-paragraphs.cbor'
#parse_annotations(file)
#make_article_corpus_v2(file)
#make_hierarchical_corpus(file)
#make_toplevel_corpus(file)
file = r'train\base.train.cbor-outlines.cbor'
#create_queries(file)
create_queries_for_expansion(file)
#create_queries_for_expansion_article(file)
#create_queries_for_expansion_toplevel(file)
#parse_paragraphs(file)
#make_big_corpus(file)
file = r'test200-train\train.pages.cbor-paragraphs.cbor'
#parse_paragraphs(file)