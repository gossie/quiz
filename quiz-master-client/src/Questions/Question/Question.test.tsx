import React from 'react';
import { render } from '@testing-library/react';
import { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from './Question';

test('should start editing', async done => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const onEdit = (questionToEdit: Question) => {
        expect(questionToEdit).toEqual(question);
        done();
    };

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} onEdit={onEdit} />);

    const editModeButton = getByTestId('edit-question-0') as HTMLButtonElement;
    editModeButton.click();
});
